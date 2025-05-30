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
import com.akilimo.mobile.databinding.ActivityFertilizersBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.FertilizerPriceResponse
import com.akilimo.mobile.entities.FertilizerResponse
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.AkilimoApi
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
import org.modelmapper.ModelMapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class InterCropFertilizersActivity : BaseActivity() {
    private val TAG: String = BaseActivity::class.java.simpleName


    var toolbar: Toolbar? = null
    var recyclerView: RecyclerView? = null
    var lyt_progress: LinearLayout? = null
    var coordinatorLayout: CoordinatorLayout? = null
    var btnSave: AppCompatButton? = null
    var btnCancel: AppCompatButton? = null
    var btnRetry: AppCompatButton? = null
    var errorImage: ImageView? = null
    var errorLabel: TextView? = null

    private var _binding: ActivityFertilizersBinding? = null
    private val binding get() = _binding!!


    private var availableFertilizersList: List<Fertilizer> = ArrayList()
    private var selectedFertilizers: MutableList<Fertilizer> = ArrayList()
    private var useCase: String = EnumUseCase.NA.name

    private var mAdapter: FertilizerGridAdapter? = null
    private val minSelection = 1
    private var modelMapper: ModelMapper? = null

    companion object {
        var useCaseTag: String = "useCase"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFertilizersBinding.inflate(layoutInflater)
        setContentView(binding.root)


        toolbar = binding.toolbarLayout.toolbar
        recyclerView = binding.availableFertilizers
        lyt_progress = binding.lytProgress
        coordinatorLayout = binding.coordinatorLayout
        btnSave = binding.twoButtons.btnFinish
        btnCancel = binding.twoButtons.btnCancel
        btnRetry = binding.btnRetry
        errorImage = binding.errorImage
        errorLabel = binding.errorLabel

        modelMapper = ModelMapper()

        val intent = intent
        if (intent != null) {
            useCase = intent.getStringExtra(useCaseTag) ?: EnumUseCase.NA.name
        }

        val profileInfo = database.profileInfoDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode
            currencyCode = profileInfo.currencyCode
        }

        initToolbar()
        initComponent()
    }

    override fun initToolbar() {
        toolbar!!.setNavigationIcon(R.drawable.ic_left_arrow)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.title_activity_fertilizer_choice)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        toolbar!!.setNavigationOnClickListener { v: View? -> validateInput(false) }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        validateInput(true)
    }

    private fun validateInput(backPressed: Boolean) {
        if (isMinSelected) {
            closeActivity(backPressed)
        }
    }


    override fun initComponent() {
        btnSave!!.text = this@InterCropFertilizersActivity.getString(R.string.lbl_finish)
        recyclerView!!.visibility = View.GONE
        recyclerView!!.layoutManager = GridLayoutManager(this, 2)
        recyclerView!!.addItemDecoration(SpacingItemDecoration(2, dpToPx(this, 3), true))
        recyclerView!!.setHasFixedSize(true)
        mAdapter = FertilizerGridAdapter(this@InterCropFertilizersActivity)
        recyclerView!!.adapter = mAdapter


        mAdapter!!.setOnItemClickListener(object : FertilizerGridAdapter.OnItemClickListener {
            override fun onItemClick(view: View, clickedFertilizer: Fertilizer, position: Int) {
                mAdapter!!.setActiveRowIndex(position)
                var selectedType = database.fertilizerDao().findOneByTypeCountryAndUseCase(
                    clickedFertilizer.fertilizerType!!, countryCode, useCase
                )
                if (selectedType == null) {
                    selectedType = clickedFertilizer
                }
                val cleanedFertilizers: MutableList<Fertilizer> = selectedFertilizers

                selectedType.countryCode = countryCode

                val arguments = Bundle()
                arguments.putParcelable(FertilizerPriceDialogFragment.FERTILIZER_TYPE, selectedType)

                val priceDialogFragment = FertilizerPriceDialogFragment()
                priceDialogFragment.arguments = arguments

                priceDialogFragment.setOnDismissListener(object : IFertilizerDismissListener {
                    override fun onDismiss(
                        priceSpecified: Boolean,
                        fertilizer: Fertilizer,
                        removeSelected: Boolean
                    ) {
                        val shouldUpdate = priceSpecified || removeSelected
                        if (!shouldUpdate) return
                        database.fertilizerDao().update(fertilizer)
                        if (removeSelected) {
                            selectedFertilizers = removeFertilizerByType(
                                cleanedFertilizers,
                                fertilizer.fertilizerType!!
                            )
                        } else {
                            selectedFertilizers.add(fertilizer)
                        }
                        validate(false)
                    }
                })


                showDialogFragmentSafely(
                    supportFragmentManager,
                    priceDialogFragment,
                    FertilizerPriceDialogFragment.ARG_ITEM_ID
                )
            }

        })

        initializeFertilizers()

        btnRetry!!.setOnClickListener { view: View? -> initializeFertilizers() }
        btnSave!!.setOnClickListener { view: View? ->
            database.adviceStatusDao().insert(
                AdviceStatus(
                    EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIM.name,
                    isMinSelected
                )
            )
            database.adviceStatusDao().insert(
                AdviceStatus(
                    EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIS.name,
                    isMinSelected
                )
            )
            if (isMinSelected) {
                closeActivity(false)
            }
        }
        btnCancel!!.setOnClickListener { view: View? -> closeActivity(false) }
    }

    override fun validate(backPressed: Boolean) {
        if (mAdapter != null) {
            availableFertilizersList =
                database.fertilizerDao().findAllByCountryAndUseCase(countryCode, useCase)
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

        val call = AkilimoApi.apiService.getFertilizers(countryCode = countryCode)
        call.enqueue(object : Callback<FertilizerResponse> {
            override fun onResponse(
                call: Call<FertilizerResponse>,
                response: Response<FertilizerResponse>
            ) {
                if (response.isSuccessful) {
                    val deletionList: MutableList<Fertilizer> = ArrayList()
                    availableFertilizersList = response.body()!!.data
                    val savedList = database.fertilizerDao().findAllByCountry(countryCode)
                    if (savedList.size > 0) {
                        for (savedFertilizer in savedList) {
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

                    database.fertilizerDao().deleteFertilizerByList(deletionList)
                    for (fertilizer in availableFertilizersList) {
                        loadFertilizerPrices(fertilizer.fertilizerKey!!)
                    }
                    validate(false)
                    lyt_progress!!.visibility = View.GONE
                    recyclerView!!.visibility = View.VISIBLE

                }
            }

            override fun onFailure(call: Call<FertilizerResponse>, t: Throwable) {
                lyt_progress!!.visibility = View.GONE
                recyclerView!!.visibility = View.GONE
                errorLabel!!.visibility = View.VISIBLE
                errorImage!!.visibility = View.VISIBLE
                btnRetry!!.visibility = View.VISIBLE
                Sentry.captureException(t)
                Toast.makeText(this@InterCropFertilizersActivity, t.message, Toast.LENGTH_SHORT)
                    .show()
            }

        })
    }

    private fun loadFertilizerPrices(fertilizerKey: String) {
        val call = AkilimoApi.apiService.getFertilizerPrices(fertilizerKey = fertilizerKey)
        call.enqueue(object : Callback<FertilizerPriceResponse> {
            override fun onResponse(
                call: Call<FertilizerPriceResponse>,
                response: Response<FertilizerPriceResponse>
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
                Toast.makeText(this@InterCropFertilizersActivity, t.message, Toast.LENGTH_SHORT)
                    .show()
            }

        })
    }

    private val isMinSelected: Boolean
        get() {
            val count = database.fertilizerDao()
                .findAllSelectedByCountryAndUseCase(countryCode, useCase).size
            if (count < minSelection) {
                val snackBar = Snackbar
                    .make(
                        coordinatorLayout!!,
                        String.format(
                            Locale.US,
                            this@InterCropFertilizersActivity.getString(R.string.lbl_min_selection),
                            minSelection
                        ),
                        Snackbar.LENGTH_LONG
                    )
                snackBar.show()
            }
            return count >= minSelection
        }
}
