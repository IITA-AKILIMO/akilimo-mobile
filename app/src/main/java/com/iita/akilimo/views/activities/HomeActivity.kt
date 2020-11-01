package com.iita.akilimo.views.activities


import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.google.android.gms.common.util.Strings
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.iita.akilimo.R
import com.iita.akilimo.adapters.ViewPagerAdapter
import com.iita.akilimo.dao.AppDatabase
import com.iita.akilimo.dao.LocationInfoDao
import com.iita.akilimo.databinding.ActivityHomeBinding
import com.iita.akilimo.entities.LocationInfo
import com.iita.akilimo.inherit.BaseActivity
import com.iita.akilimo.interfaces.IFragmentCallBack
import com.iita.akilimo.utils.AppUpdateHelper
import com.iita.akilimo.utils.Tools
import com.iita.akilimo.views.activities.usecases.RecommendationsActivity
import com.iita.akilimo.views.fragments.*
import kotlin.system.exitProcess


@Suppress("SENSELESS_COMPARISON")
class HomeActivity : BaseActivity(), IFragmentCallBack {
    companion object {
        const val MAP_BOX_PLACE_PICKER_REQUEST_CODE = 208
    }


    private var defaultPlaceName: String = ""

    private val fragmentArray = mutableSetOf<Fragment>()
    private val maxStep = 0

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewPager: ViewPager
    private lateinit var myViewPagerAdapter: ViewPagerAdapter
    private lateinit var btnStart: Button

    private var exit: Boolean = false
    private var currentPosition: Int = 0

    private var showProceedButton: Boolean = true
    private var currentLat: Double = 0.toDouble()
    private var currentLong: Double = 0.toDouble()
    private var currentAlt: Double = 0.toDouble()
    private var placeName: String? = null
    private var address: String? = null
    private lateinit var currentFragment: Fragment
    private var location: LocationInfo? = null


    private lateinit var activity: Activity
    private lateinit var appUpdateHelper: AppUpdateHelper
    private lateinit var appUpdater: AppUpdater

//    override fun onAttachFragment(fragment: Fragment) {
//        when (fragment) {
//            is SummaryFragment -> {
//                fragment.setOnFragmentCloseListener(this)
//            }
//            is BioDataFragment -> {
//                fragment.setOnFragmentCloseListener(this)
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activity = this
        context = this
        database = AppDatabase.getDatabase(context)

        defaultPlaceName = getString(R.string.lbl_place_name)
        viewPager = binding.homeViewPager
        btnStart = binding.btnGetStarted

        //Add the various fragments
        val bioDataFragment = BioDataFragment.newInstance()
        fragmentArray.add(WelcomeFragment.newInstance())
        fragmentArray.add(InfoFragment.newInstance())
        //fragmentArray.add(PrivacyStatementFragment.newInstance())

        fragmentArray.add(bioDataFragment)
        fragmentArray.add(CountryFragment.newInstance())
        fragmentArray.add(LocationFragment.newInstance())
        fragmentArray.add(FieldInfoFragment.newInstance())
        fragmentArray.add(AreaUnitFragment.newInstance())
        fragmentArray.add(FieldSizeFragment.newInstance())
        fragmentArray.add(CurrentPracticeFragment.newInstance())
        fragmentArray.add(SummaryFragment.newInstance())


        //check updates
        appUpdateHelper = AppUpdateHelper(this)
        appUpdater = appUpdateHelper
            .showUpdateMessage(Display.DIALOG)
            .setButtonDoNotShowAgain("")
        appUpdater.start()
        //add bottom progress dots
        currentPosition = fragmentArray.indexOf(bioDataFragment)
        bottomProgressDots(0)
        initComponent()

    }

    fun onDataSaved() {
        viewPager.currentItem = currentPosition + 1
    }

    fun onFragmentClose(hideButton: Boolean) {
        showProceedButton = hideButton
        when {
            !hideButton -> {
                btnStart.visibility = View.VISIBLE
                btnStart.text = getString(R.string.lbl_proceed)
            }
            else -> btnStart.visibility = View.GONE
        }
    }
    override fun initToolbar() {
        throw UnsupportedOperationException()
    }

    override fun validate(backPressed: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun initComponent() {

        val rationale: String = getString(R.string.lbl_permission_rationale)

        checkAppPermissions(rationale)
        fetchFireBaseConfig(this)
        btnStart.visibility = View.GONE

        myViewPagerAdapter = ViewPagerAdapter(supportFragmentManager, 0, fragmentArray)

        viewPager.adapter = myViewPagerAdapter
        viewPager.offscreenPageLimit = 1
        Tools.setSystemBarColor(activity, R.color.deep_purple_600)


        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                    newPosition: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
            ) {
                try {
                    val activeFragment: Fragment = fragmentArray.elementAt(newPosition)

//                    (activeFragment as? CurrentPracticeFragment)?.refreshData()
//
//                    (activeFragment as? CountryFragment)?.refreshData()
//
//                    (activeFragment as? AreaUnitFragment)?.refreshData()
//
//                    (activeFragment as? FieldSizeFragment)?.refreshData()
//
//                    (activeFragment as? LocationFragment)?.refreshData()
//
//                    (activeFragment as? SummaryFragment)?.refreshData()

//                    (activeFragment as? BioDataFragment)?.refreshData()

                    currentFragment = activeFragment
                } catch (ex: Exception) {
                    FirebaseCrashlytics.getInstance().log(ex.message!!)
                    FirebaseCrashlytics.getInstance().recordException(ex)
                }
            }

            override fun onPageSelected(position: Int) {
                bottomProgressDots(position)

                Tools.setSystemBarColor(activity, R.color.colorAccent)
                //stop the updater
                appUpdater.stop();
                btnStart.visibility = View.GONE
                when (position) {
                    0 -> {
                        appUpdater.start()
                        btnStart.visibility = View.GONE
                        //Tools.setSystemBarColor(activity, R.color.green_700)
                    }
                    fragmentArray.size - 1 -> {
                        if (showProceedButton) {
                            btnStart.visibility = View.GONE
                        }
                    }
                }
            }

        })

        btnStart.setOnClickListener {
            appUpdater.start()
            val intent = Intent(this, RecommendationsActivity::class.java)
            startActivity(intent)
            openActivity()
        }
    }

    private fun bottomProgressDots(currentIndex: Int) {
        val dotsLayout = findViewById<LinearLayout>(R.id.homeLayoutDots)
        val dots = arrayOfNulls<ImageView>(fragmentArray.size)
        val widthHeight = 15

        dotsLayout.removeAllViews()
        for (dotIndex in dots.indices) {
            dots[dotIndex] = ImageView(this)

            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams(widthHeight, widthHeight))

            params.setMargins(10, 10, 10, 10)
            dots[dotIndex]?.layoutParams = params
            dots[dotIndex]?.setImageResource(R.drawable.shape_rect_outline)
            dots[dotIndex]?.setColorFilter(
                    ContextCompat.getColor(this, R.color.grey_20), PorterDuff.Mode.SRC_IN
            )
            dotsLayout.addView(dots[dotIndex])
        }

        if (dots.isNotEmpty()) {
            dots[currentIndex]?.setImageResource(R.drawable.shape_rect_outline)
            dots[currentIndex]?.setColorFilter(
                    ContextCompat.getColor(
                            this,
                            R.color.colorPrimary
                    ), PorterDuff.Mode.SRC_IN
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == MAP_BOX_PLACE_PICKER_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        currentLat = data.getDoubleExtra(MapBoxActivity.LAT, 0.0)
                        currentLong = data.getDoubleExtra(MapBoxActivity.LON, 0.0)
                        currentAlt = data.getDoubleExtra(MapBoxActivity.ALT, 0.0)
                        placeName = data.getStringExtra(MapBoxActivity.PLACE_NAME)

                    } else {
                        Toast.makeText(
                                context,
                                getString(R.string.lbl_location_error),
                                Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            location = database.locationInfoDao().findOne()
            if (location == null) {
                location = LocationInfo()
            }
            location?.latitude = currentLat
            location?.longitude = currentLong
            location?.altitude = currentAlt
            location?.placeName = when {
                !Strings.isEmptyOrWhitespace(placeName) -> placeName
                else -> defaultPlaceName
            }
            location?.address = when {
                !Strings.isEmptyOrWhitespace(address) -> address
                else -> "NA"
            }

            val locationInfoDao: LocationInfoDao = database.locationInfoDao()
            locationInfoDao.insert(location!!)
//            (currentFragment as? LocationFragment)?.refreshData()
        } catch (ex: Exception) {
            Toast.makeText(
                    context,
                    ex.message,
                    Toast.LENGTH_LONG
            ).show()
            FirebaseCrashlytics.getInstance().log(ex.message!!)
            FirebaseCrashlytics.getInstance().recordException(ex)
        }

    }

    override fun onBackPressed() {
        try {
            if (exit) {
                finish() // finish activity
                System.gc() //run the garbage collector to free up memory resources
                exitProcess(0) //exit the system
            } else {
                Toast.makeText(
                        this,
                        getString(R.string.lbl_exit_tip),
                        Toast.LENGTH_SHORT
                ).show()
                exit = true
                Handler().postDelayed({ exit = false }, (3 * 1000).toLong())
            }
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
            FirebaseCrashlytics.getInstance().log(ex.message!!)
            FirebaseCrashlytics.getInstance().recordException(ex)
        }
    }

    override fun reloadView() {
        TODO("Not yet implemented")
    }
}
