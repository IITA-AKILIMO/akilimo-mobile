package com.akilimo.mobile.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemPriceOptionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A generic adapter that efficiently displays and handles item selection
 * for any Room entity type using DiffUtil for optimal performance.
 *
 * @param T The type of items to display
 * @param getId Function to extract unique ID from item
 * @param getLabel Function to extract display text from item
 * @param isSelected Function to check if item is initially selected
 * @param onItemClick Callback when item is clicked
 */
class GenericSelectableAdapter<T : Any>(
    private val scope: CoroutineScope,
    private val getId: (T) -> Int,
    private val getLabel: (T) -> String,
    private val isSelected: (T) -> Boolean,
    private val isExactPrice: (T) -> Boolean = { false },
    private val onItemClick: (T) -> Unit,
    private val onExactAmount: ((item: T, value: Double) -> Unit)? = null,
    private val getExactPrice: (T) -> Double? = { null },
) : ListAdapter<T, GenericSelectableAdapter<T>.ViewHolder>(GenericDiffCallback(getId, getLabel)) {

    private var selectedId: Int? = null

    init {
        // Enable stable IDs for better performance
        setHasStableIds(true)
    }

    inner class ViewHolder(private val binding: ItemPriceOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var debounceJob: Job? = null

        fun bind(item: T) {
            val ctx = binding.root.context
            val id = getId(item)
            val label = getLabel(item)
            val isExact = isExactPrice(item)
            val exactValue = getExactPrice(item)
            val isSelectedItem = id == selectedId

            binding.priceText.text = label
            binding.priceInput.setText(
                if (exactValue != null && exactValue > 0.0) exactValue.toString() else ""
            )
            if (!isExact) {
                updateSelectionState(isSelectedItem)
            }
            binding.priceInputLayout.visibility =
                if (isSelectedItem && isExact) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                val previousSelectedId = selectedId
                selectedId = id
                val isCustom = isExactPrice(item)

                notifyItemChangedIfExists(previousSelectedId)
                notifyItemChangedIfExists(id)

                if (isCustom) {
                    binding.priceInputLayout.visibility = View.VISIBLE
                    binding.priceInput.requestFocus()
                } else {
                    binding.priceInputLayout.visibility = View.GONE
                    onItemClick(item)
                }
            }

            binding.priceInput.doAfterTextChanged { text ->
                if (!isExact || binding.priceInputLayout.visibility != View.VISIBLE) return@doAfterTextChanged

                debounceJob?.cancel()
                debounceJob = scope.launch {
                    delay(500)
                    val raw = text?.toString()?.trim().orEmpty()
                    val normalized = raw.replace(',', '.')
                    val value = normalized.toDoubleOrNull()
                    withContext(Dispatchers.Main) {
                        if (value == null || value <= 0.0) {
                            binding.priceInput.error =
                                ctx.getString(R.string.lbl_provide_valid_unit_price)
                        } else {
                            binding.priceInput.error = null
                            onExactAmount?.invoke(item, value)
                        }
                    }
                }
            }
        }

        private fun updateSelectionState(isSelected: Boolean) {
            binding.selectedIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
            binding.root.isSelected = isSelected
        }

        private fun notifyItemChangedIfExists(id: Int?) {
            id?.let { itemId ->
                val position = currentList.indexOfFirst { getId(it) == itemId }
                if (position != -1) notifyItemChanged(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPriceOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long = getId(getItem(position)).toLong()

    /**
     * Updates the list with new items and preserves or updates selection.
     * Uses DiffUtil for efficient updates.
     */
    fun updateItems(newItems: List<T>) {
        // Update selected ID based on new items if not already set
        if (selectedId == null) {
            selectedId = newItems.firstOrNull { isSelected(it) }?.let { getId(it) }
        }
        submitList(newItems)
    }

    /**
     * Updates the selected item programmatically.
     */
    fun setSelectedItem(id: Int) {
        val previousSelectedId = selectedId
        selectedId = id

        // Notify only affected items
        currentList.indexOfFirst { getId(it) == previousSelectedId }.takeIf { it != -1 }?.let {
            notifyItemChanged(it)
        }
        currentList.indexOfFirst { getId(it) == id }.takeIf { it != -1 }?.let {
            notifyItemChanged(it)
        }
    }

    /**
     * Gets the currently selected item, if any.
     */
    fun getSelectedItem(): T? = currentList.firstOrNull { getId(it) == selectedId }

    /**
     * Clears the current selection.
     */
    fun clearSelection() {
        selectedId?.let { id ->
            selectedId = null
            currentList.indexOfFirst { getId(it) == id }.takeIf { it != -1 }?.let {
                notifyItemChanged(it)
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates.
     */
    private class GenericDiffCallback<T : Any>(
        private val getId: (T) -> Int,
        private val getLabel: (T) -> String
    ) : DiffUtil.ItemCallback<T>() {

        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return getId(oldItem) == getId(newItem)
        }

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return getLabel(oldItem) == getLabel(newItem)
        }
    }
}