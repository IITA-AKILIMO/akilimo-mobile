package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.FertilizerGridAdapter
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.databinding.ActivityFertilizersBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.FertilizerPriceResponse
import com.akilimo.mobile.entities.FertilizerResponse
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.AkilimoService
import com.akilimo.mobile.interfaces.IFertilizerDismissListener
import com.akilimo.mobile.utils.FertilizerList.removeFertilizerByType
import com.akilimo.mobile.utils.Tools.dpToPx
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.views.fragments.dialog.FertilizerPriceDialogFragment
import com.akilimo.mobile.widget.SpacingItemDecoration
import com.google.android.material.snackbar.Snackbar
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class FertilizersActivity : BaseActivity() {
    var myToolbar: Toolbar? = null
    var recyclerView: RecyclerView? = null
    private var lyt_progress: LinearLayout? = null
    private var coordinatorLayout: CoordinatorLayout? = null
    private var btnSave: AppCompatButton? = null
    var btnCancel: AppCompatButton? = null
    var btnRetry: AppCompatButton? = null
    var errorImage: ImageView? = null
    var errorLabel: TextView? = null


    private var _binding: ActivityFertilizersBinding? = null
    private val binding get() = _binding!!

    private lateinit var akilimoService: AkilimoService


    private var availableFertilizersList: MutableList<Fertilizer> = ArrayList()
    private var selectedFertilizers: MutableList<Fertilizer> = ArrayList()
    private var mAdapter: FertilizerGridAdapter? = null
    private var minSelection: Int = 2
    private var useCase: String = EnumUseCase.NA.name

    companion object {
        var useCaseTag: String = "useCase"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFertilizersBinding.inflate(layoutInflater)
        akilimoService = AkilimoApi.apiService

        setContentView(binding.root)

        myToolbar = binding.toolbarLayout.toolbar
        recyclerView = binding.availableFertilizers
        lyt_progress = binding.lytProgress
        coordinatorLayout = binding.coordinatorLayout
        btnSave = binding.twoButtons.btnFinish
        btnCancel = binding.twoButtons.btnCancel
        btnRetry = binding.btnRetry
        errorImage = binding.errorImage
        errorLabel = binding.errorLabel

        val intent = intent
        if (intent != null) {
            useCase = intent.getStringExtra(useCaseTag) ?: EnumUseCase.NA.name
        }


        val profileInfo = database.profileInfoDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode
            currencyCode = profileInfo.currencyCode
        }

        setupToolbar(binding.toolbarLayout.toolbar, R.string.title_activity_fertilizer_choice) {
            validateInput(false)
        }

        initComponent()
    }

    override fun initToolbar() {
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        validateInput(true)
    }

    private fun validateInput(backPressed: Boolean) {
        if (isMinSelected()) {
            closeActivity(backPressed)
        }
    }


    override fun initComponent() {
        recyclerView!!.visibility = View.GONE
        recyclerView!!.layoutManager = GridLayoutManager(this, 2)
        recyclerView!!.addItemDecoration(SpacingItemDecoration(2, dpToPx(this, 3), true))
        recyclerView!!.setHasFixedSize(true)
        mAdapter = FertilizerGridAdapter(this@FertilizersActivity)
        recyclerView!!.adapter = mAdapter

        btnSave!!.text = this@FertilizersActivity.getString(R.string.lbl_finish)

        val database = getDatabase(this@FertilizersActivity)

        mAdapter!!.setOnItemClickListener(object : FertilizerGridAdapter.OnItemClickListener {
            override fun onItemClick(view: View, clickedFertilizer: Fertilizer, position: Int) {
                mAdapter!!.setActiveRowIndex(position)
                var selectedType = database.fertilizerDao().findOneByTypeCountryAndUseCase(
                    clickedFertilizer.fertilizerType.toString(), countryCode, useCase
                )
                if (selectedType == null) {
                    selectedType = clickedFertilizer
                }
                //let us open the price dialog now
                val cleanedFertilizers = selectedFertilizers
                selectedType.countryCode = countryCode

                val arguments = Bundle()
                arguments.putParcelable(FertilizerPriceDialogFragment.FERTILIZER_TYPE, selectedType)

                val priceDialogFragment = FertilizerPriceDialogFragment()
                priceDialogFragment.arguments = arguments

                priceDialogFragment.setOnDismissListener(object : IFertilizerDismissListener {
                    override fun onDismiss(
                        priceSpecified: Boolean, fertilizer: Fertilizer, removeSelected: Boolean
                    ) {
                        val shouldUpdate = priceSpecified || removeSelected
                        if (!shouldUpdate) return
                        database.fertilizerDao().update(fertilizer)

                        if (removeSelected) {
                            selectedFertilizers = removeFertilizerByType(
                                cleanedFertilizers, fertilizer.fertilizerType!!
                            )
                        } else {
                            selectedFertilizers.add(fertilizer)
                        }

                        validate(false)

                    }
                })

                showDialogFragmentSafely(
                    fragmentManager = supportFragmentManager,
                    dialogFragment = priceDialogFragment,
                    FertilizerPriceDialogFragment.ARG_ITEM_ID
                )
            }
        })

        initializeFertilizers()

        btnRetry!!.setOnClickListener { view: View? -> initializeFertilizers() }
        btnSave!!.setOnClickListener { view: View? ->
            database.adviceStatusDao().insert(
                AdviceStatus(
                    EnumAdviceTasks.AVAILABLE_FERTILIZERS.name, isMinSelected()
                )
            )
            if (isMinSelected()) {
                closeActivity(false)
            }
        }
        btnCancel!!.setOnClickListener { view: View? -> closeActivity(false) }
    }

    override fun validate(backPressed: Boolean) {
        if (mAdapter != null) {
            availableFertilizersList = database.fertilizerDao().findAllByCountry(countryCode)
            mAdapter!!.setItems(availableFertilizersList)
        }
    }

    private fun initializeFertilizers() {
        lyt_progress!!.visibility = View.VISIBLE
        lyt_progress!!.alpha = 1.0f
        recyclerView!!.visibility = View.GONE
        errorLabel!!.visibility = View.GONE
        errorImage!!.visibility = View.GONE
        btnRetry!!.visibility = View.GONE


        availableFertilizersList = database.fertilizerDao().findAllByCountry(countryCode)
        if (availableFertilizersList.isNotEmpty()) {
            mAdapter!!.setItems(availableFertilizersList)
            lyt_progress!!.visibility = View.GONE
            recyclerView!!.visibility = View.VISIBLE
            return
        }
        val call = akilimoService.getFertilizers(countryCode = countryCode)
        call.enqueue(object : Callback<FertilizerResponse> {
            override fun onResponse(
                call: Call<FertilizerResponse>, response: Response<FertilizerResponse>
            ) {
                val deletionList: MutableList<Fertilizer> = ArrayList()
                if (response.isSuccessful) {
                    val availableFertilizersList = response.body()!!.data
                    val savedList = database.fertilizerDao().findAllByCountry(countryCode)
                    if (availableFertilizersList.isNotEmpty()) {
                        if (savedList.isNotEmpty()) {
                            for (savedFertilizer in savedList) {
                                // Loop arrayList1 items
                                var found = false
                                for (latestFertilizer in availableFertilizersList) {
                                    val updateFertilizer = database.fertilizerDao()
                                        .findByType(latestFertilizer.fertilizerType)
                                    if (updateFertilizer != null) {
                                        updateFertilizer.available = latestFertilizer.available
                                        database.fertilizerDao().update(updateFertilizer)
                                    } else {
                                        database.fertilizerDao().insert(latestFertilizer)
                                    }
                                    if (latestFertilizer.fertilizerType == savedFertilizer.fertilizerType) {
                                        found = true
                                    }
                                }
                                if (!found) {
                                    deletionList.add(savedFertilizer)
                                }
                            }
                        } else {
                            database.fertilizerDao().insertAll(availableFertilizersList)
                        }
                    }
                    database.fertilizerDao().deleteFertilizerByList(deletionList)
                    for (fertilizer in availableFertilizersList) {
                        loadFertilizerPrices(fertilizer.fertilizerKey!!)
                    }
                    validate(false)
                    lyt_progress!!.visibility = View.GONE
                    recyclerView!!.visibility = View.VISIBLE
                } else {
                    lyt_progress!!.visibility = View.GONE
                    recyclerView!!.visibility = View.GONE
                    errorLabel!!.visibility = View.VISIBLE
                    errorImage!!.visibility = View.VISIBLE
                    btnRetry!!.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<FertilizerResponse>, t: Throwable) {
                lyt_progress!!.visibility = View.GONE
                recyclerView!!.visibility = View.GONE
                errorLabel!!.visibility = View.VISIBLE
                errorImage!!.visibility = View.VISIBLE
                btnRetry!!.visibility = View.VISIBLE
                Sentry.captureException(t)
                Toast.makeText(this@FertilizersActivity, t.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun loadFertilizerPrices(fertilizerKey: String) {
        val call = akilimoService.getFertilizerPrices(fertilizerKey = fertilizerKey)
        call.enqueue(object : Callback<FertilizerPriceResponse> {
            override fun onResponse(
                call: Call<FertilizerPriceResponse>, response: Response<FertilizerPriceResponse>
            ) {
                if (response.isSuccessful) {
                    lyt_progress!!.visibility = View.GONE
                    val fertilizerPricesList = response.body()!!.data
                    database.fertilizerPriceDao().insertAll(fertilizerPricesList)
                } else {
                    lyt_progress!!.visibility = View.GONE
                    recyclerView!!.visibility = View.GONE
                    errorLabel!!.visibility = View.VISIBLE
                    errorImage!!.visibility = View.VISIBLE
                    btnRetry!!.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<FertilizerPriceResponse>, t: Throwable) {
                lyt_progress!!.visibility = View.GONE
                recyclerView!!.visibility = View.GONE
                errorLabel!!.visibility = View.VISIBLE
                errorImage!!.visibility = View.VISIBLE
                btnRetry!!.visibility = View.VISIBLE
                Sentry.captureException(t)
                Toast.makeText(this@FertilizersActivity, t.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun isMinSelected(): Boolean {
        val count = database.fertilizerDao().findAllSelectedByCountry(countryCode).size
        if (count < minSelection) {
            val snackBar = Snackbar.make(
                lyt_progress!!, String.format(
                    Locale.US,
                    this@FertilizersActivity.getString(R.string.lbl_min_selection),
                    minSelection
                ), Snackbar.LENGTH_SHORT
            )
            snackBar.show()
        }
        return count >= minSelection
    }
}
