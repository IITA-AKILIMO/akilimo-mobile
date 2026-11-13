package com.akilimo.mobile.ui.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.R
import com.akilimo.mobile.entities.SavedAdvice
import com.akilimo.mobile.repos.account.AccountResult
import com.akilimo.mobile.repos.account.AdviceFilter
import com.akilimo.mobile.repos.account.SavedAdviceRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * The saved-advice list. It is built for a long history: the list is read from the phone a screenful at a time and grows as
 * the farmer scrolls, can be narrowed by use case and country, and is brought up to date from the account in the background.
 *
 * Deleting works like a mail inbox: swiping an item or deleting a selection hides it at once and shows an Undo; the
 * request goes to the server a few seconds later, or when the farmer leaves the screen, unless it was undone.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SavedAdviceViewModel @Inject constructor(
    private val repo: SavedAdviceRepo,
) : ViewModel() {

    data class UiState(
        val refreshing: Boolean = false,
        val deleting: Boolean = false,
        @param:StringRes val messageRes: Int? = null,
        val filter: AdviceFilter = AdviceFilter(),
        /** How many items the list shows now; it grows by [PAGE_SIZE] each time the farmer reaches the end. */
        val limit: Int = PAGE_SIZE,
        /** The items the farmer has picked (long press, then tap); empty when not selecting. */
        val selected: Set<String> = emptySet(),
    ) {
        val selecting: Boolean get() = selected.isNotEmpty()
    }

    /** Something the screen should tell the farmer about. */
    sealed interface Event {
        /** [count] items were hidden and will be deleted soon; [batchId] undoes exactly that batch. */
        data class Deleted(val batchId: Long, val count: Int) : Event

        /** Open the share sheet with this text (a left swipe on an item). */
        data class Share(val text: String) : Event
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = EVENT_BUFFER)
    val events: SharedFlow<Event> = _events.asSharedFlow()

    /** Ids hidden from the list while their delete waits out the undo time. */
    private val pending = MutableStateFlow<Set<String>>(emptySet())
    private val batches = mutableMapOf<Long, Set<String>>()
    private val jobs = mutableMapOf<Long, Job>()
    private var nextBatch = 1L

    private val view = _uiState.map { it.filter to it.limit }.distinctUntilChanged()

    /** What the phone holds for this screenful, including items waiting to be deleted. */
    private val loaded: StateFlow<List<SavedAdvice>?> = view
        .flatMapLatest { (filter, limit) -> repo.observePage(filter, limit) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /** The list as shown: null until the first read of the phone's copy, without items that were just deleted. */
    val items: StateFlow<List<SavedAdvice>?> = combine(loaded, pending) { list, hidden ->
        list?.filterNot { it.requestId in hidden }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    private val totalCount: StateFlow<Int> = _uiState.map { it.filter }.distinctUntilChanged()
        .flatMapLatest { repo.observeCount(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0)

    /** All items that match the filter, not only the ones on screen, less the ones just deleted. */
    val count: StateFlow<Int> = combine(totalCount, loaded, pending) { total, list, hidden ->
        val hiddenHere = list.orEmpty().count { it.requestId in hidden }
        (total - hiddenHere).coerceAtLeast(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0)

    val useCases: StateFlow<List<String>> = repo.observeUseCases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val countries: StateFlow<List<String>> = repo.observeCountries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        if (_uiState.value.refreshing) return
        viewModelScope.launch {
            _uiState.update { it.copy(refreshing = true, messageRes = null) }
            val failed = repo.refresh() is AccountResult.Failure
            // Offline is normal in the field: say so quietly, the phone's copy is still shown
            _uiState.update { it.copy(refreshing = false, messageRes = if (failed) R.string.saved_advice_refresh_failed else null) }
        }
    }

    /** The farmer reached the end of what is shown: show another [PAGE_SIZE], if there is more. */
    fun loadMore() {
        val state = _uiState.value
        val shown = loaded.value?.size ?: return
        if (shown >= state.limit && state.limit < totalCount.value) {
            _uiState.update { it.copy(limit = it.limit + PAGE_SIZE) }
        }
    }

    fun setUseCase(useCase: String?) = _uiState.update {
        it.copy(filter = it.filter.copy(useCase = useCase), limit = PAGE_SIZE, selected = emptySet())
    }

    fun setCountry(country: String?) = _uiState.update {
        it.copy(filter = it.filter.copy(country = country), limit = PAGE_SIZE, selected = emptySet())
    }

    // ── Selecting ─────────────────────────────────────────────────────────────

    /** Long press starts a selection with this item; a tap in selection mode adds or removes one. */
    fun toggleSelected(requestId: String) = _uiState.update {
        it.copy(selected = if (requestId in it.selected) it.selected - requestId else it.selected + requestId)
    }

    /** Selects every item on screen (what has been loaded). "Delete all" in the menu covers the whole history. */
    fun selectAll() {
        val ids = items.value.orEmpty().map { it.requestId }.toSet()
        _uiState.update { it.copy(selected = ids) }
    }

    fun clearSelection() = _uiState.update { it.copy(selected = emptySet()) }

    // ── Sharing ───────────────────────────────────────────────────────────────

    /**
     * Left swipe: share the advice text. If the phone does not have the text yet it is fetched first (the list carries none);
     * when that fails the farmer is told, and nothing is shared.
     */
    fun share(requestId: String) {
        val text = loaded.value?.firstOrNull { it.requestId == requestId }?.recommendation
        if (text != null) {
            _events.tryEmit(Event.Share(text))
            return
        }
        viewModelScope.launch {
            when (val result = repo.loadText(requestId)) {
                is AccountResult.Failure -> _uiState.update { it.copy(messageRes = result.error.messageRes()) }
                is AccountResult.Success -> {
                    val loadedText = repo.observe(requestId).first()?.recommendation
                    if (loadedText != null) {
                        _events.tryEmit(Event.Share(loadedText))
                    } else {
                        _uiState.update { it.copy(messageRes = R.string.saved_advice_text_unavailable) }
                    }
                }
            }
        }
    }

    // ── Deleting, with undo ───────────────────────────────────────────────────

    /** Swipe: delete one item (hidden now, deleted in a few seconds unless undone). */
    fun delete(requestId: String) = hideAndDelete(setOf(requestId))

    /** Delete every selected item the same way. */
    fun deleteSelected() = hideAndDelete(_uiState.value.selected)

    private fun hideAndDelete(ids: Set<String>) {
        if (ids.isEmpty()) return
        val batchId = nextBatch++
        batches[batchId] = ids
        pending.update { it + ids }
        _uiState.update { it.copy(selected = it.selected - ids, messageRes = null) }
        _events.tryEmit(Event.Deleted(batchId, ids.size))
        jobs[batchId] = viewModelScope.launch {
            delay(UNDO_MILLIS)
            commit(batchId)
        }
    }

    /** Brings the items of one batch back, if they were not sent to the server yet. */
    fun undo(batchId: Long) {
        jobs.remove(batchId)?.cancel()
        batches.remove(batchId)?.let { ids -> pending.update { it - ids } }
    }

    /** The farmer left the screen: send what is waiting now rather than lose it. */
    fun flushPending() {
        val waiting = batches.keys.toList()
        if (waiting.isEmpty()) return
        waiting.forEach { jobs.remove(it)?.cancel() }
        viewModelScope.launch { withContext(NonCancellable) { waiting.forEach { commit(it) } } }
    }

    private suspend fun commit(batchId: Long) {
        val ids = batches[batchId] ?: return
        val result = repo.deleteMany(ids)
        batches.remove(batchId)
        jobs.remove(batchId)
        // Done: the rows are gone from the phone. Failed: the items come back, with a message
        pending.update { it - ids }
        if (result is AccountResult.Failure) {
            _uiState.update { it.copy(messageRes = result.error.messageRes()) }
        }
    }

    /** Delete the whole history from the menu (after a confirmation). */
    fun deleteAll() {
        if (_uiState.value.deleting) return
        viewModelScope.launch {
            _uiState.update { it.copy(deleting = true, messageRes = null) }
            when (val result = repo.deleteAll()) {
                is AccountResult.Failure ->
                    _uiState.update { it.copy(deleting = false, messageRes = result.error.messageRes()) }
                is AccountResult.Success ->
                    _uiState.update { it.copy(deleting = false, filter = AdviceFilter(), limit = PAGE_SIZE, selected = emptySet()) }
            }
        }
    }

    companion object {
        const val PAGE_SIZE = 50

        /** How long a delete can be undone before the request is sent. */
        const val UNDO_MILLIS = 5_000L

        private const val EVENT_BUFFER = 8
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
