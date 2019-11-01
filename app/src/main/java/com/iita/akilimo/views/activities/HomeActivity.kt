package com.iita.akilimo.views.activities


import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import butterknife.BindString
import butterknife.ButterKnife
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.crashlytics.android.Crashlytics
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.google.android.gms.common.util.Strings
import com.iita.akilimo.R
import com.iita.akilimo.adapters.ViewPagerAdapter
import com.iita.akilimo.entities.MandatoryInfo
import com.iita.akilimo.inherit.BaseActivity
import com.iita.akilimo.interfaces.IFragmentCallBack
import com.iita.akilimo.utils.AppUpdateHelper
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor
import com.iita.akilimo.views.fragments.*
import timber.log.Timber
import kotlin.system.exitProcess


class HomeActivity : BaseActivity(), IFragmentCallBack {
    companion object {
        const val MAP_BOX_PLACE_PICKER_REQUEST_CODE = 208
    }


    @BindString(R.string.welcome_title)
    private val welcomeTitle: String = ""
    @BindString(R.string.welcome_instructions)
    private val instructions: String = ""

    @BindString(R.string.lbl_permission_rationale)
    internal var rationale: String? = null
    @BindString(R.string.lbl_place_name)
    internal var defaultPlaceName: String? = null

    private val fragmentArray = mutableSetOf<Fragment>()
    private val maxStep = 0

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
    private lateinit var location: MandatoryInfo


    private lateinit var activity: Activity
    private lateinit var appUpdateHelper: AppUpdateHelper
    private lateinit var appUpdater: AppUpdater

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is SummaryFragment) {
            fragment.setOnFragmentCloseListener(this)
        }
        if (fragment is BioDataFragment) {
            fragment.setOnFragmentCloseListener(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        ButterKnife.bind(this)
        activity = this
        context = this
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(this)

        viewPager = findViewById(R.id.homeViewPager)
        btnStart = findViewById(R.id.btnGetStarted)

        //Add the various fragments
        val bioDataFragment = BioDataFragment.newInstance()
        fragmentArray.add(WelcomeFragment.newInstance())
        fragmentArray.add(bioDataFragment)
        fragmentArray.add(CountryFragment.newInstance())
        fragmentArray.add(LocationFragment.newInstance())
        fragmentArray.add(AreaUnitFragment.newInstance())
        fragmentArray.add(FieldSizeFragment.newInstance())
        fragmentArray.add(SummaryFragment.newInstance())


        //check updates
        appUpdateHelper = AppUpdateHelper(this)
        appUpdater = appUpdateHelper.showUpdateMessage(Display.DIALOG).setButtonDoNotShowAgain("")
        appUpdater.start()
        //add bottom progress dots
        currentPosition = fragmentArray.indexOf(bioDataFragment)
        bottomProgressDots(0)
        initComponent()

    }

    override fun onDataSaved() {
        //load the next fragment
        viewPager.currentItem = currentPosition + 1
    }

    override fun onFragmentClose(hideButton: Boolean) {
        showProceedButton = hideButton
        when {
            !hideButton -> {
                btnStart.visibility = View.VISIBLE
                btnStart.text = getString(R.string.lbl_proceed)
            }
            else -> btnStart.visibility = View.GONE
        }
    }

    override fun sendResult(requestCode: Int, obj: Any) {
        throw UnsupportedOperationException()
    }

    override fun initToolbar() {
        throw UnsupportedOperationException()
    }

    override fun validate(backPressed: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun initComponent() {
        checkAppPermissions(rationale)
        btnStart.visibility = View.GONE

        myViewPagerAdapter = ViewPagerAdapter(supportFragmentManager, 0, fragmentArray)

        viewPager.adapter = myViewPagerAdapter
        viewPager.offscreenPageLimit = 1


        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                newPosition: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                val activeFragment: Fragment = fragmentArray.elementAt(newPosition)

                (activeFragment as? CountryFragment)?.refreshData()

                (activeFragment as? AreaUnitFragment)?.refreshData()

                (activeFragment as? FieldSizeFragment)?.refreshData()

                (activeFragment as? LocationFragment)?.refreshData()

                (activeFragment as? SummaryFragment)?.refreshData()

                (activeFragment as? BioDataFragment)?.refreshData()

//                if (fragment is BioDataFragment) {
//                    profileDataValid = fragment.saveBioData()
//                }
//
//                if (!profileDataValid && currentPosition < newPosition) {
//                    viewPager.currentItem = currentPosition
//                }
                currentFragment = activeFragment
            }

            override fun onPageSelected(position: Int) {
                bottomProgressDots(position)
//                Tools.setSystemBarColor(activity, R.color.deep_orange_500)
                btnStart.visibility = View.GONE
                when (position) {
                    0 -> {
                        appUpdater.start()
                        btnStart.visibility = View.GONE
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
            Animatoo.animateShrink(this)
        }
    }

    private fun bottomProgressDots(currentIndex: Int) {
        val dotsLayout = findViewById<LinearLayout>(R.id.homeLayoutDots)
        val dots = arrayOfNulls<ImageView>(fragmentArray.size)
        val widthHeight = 15

        dotsLayout.removeAllViews()
        for (i in dots.indices) {
            dots[i] = ImageView(this)

            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams(widthHeight, widthHeight))

            params.setMargins(10, 10, 10, 10)
            dots[i]?.layoutParams = params
            dots[i]?.setImageResource(R.drawable.shape_circle)
            dots[i]?.setColorFilter(
                ContextCompat.getColor(this, R.color.grey_20), PorterDuff.Mode.SRC_IN
            )
            dotsLayout.addView(dots[i])
        }

        if (dots.isNotEmpty()) {
            dots[currentIndex]?.setImageResource(R.drawable.shape_circle)
            dots[currentIndex]?.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.colorAccent
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
                            "Unable to fetch your current location, please try again",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Timber.d(currentLong.toString())
                }
            }

            location = objectBoxEntityProcessor.mandatoryInfo
            location.latitude = currentLat
            location.longitude = currentLong
            location.altitude = currentAlt

            location.placeName = when {
                !Strings.isEmptyOrWhitespace(placeName) -> placeName
                else -> defaultPlaceName
            }
            location.address = when {
                !Strings.isEmptyOrWhitespace(address) -> address
                else -> "NA"
            }

            val id = objectBoxEntityProcessor.saveMandatoryInfo(location)
            if (id > 0) {
                //refresh fragment data
                (currentFragment as? LocationFragment)?.refreshData()
            }
        } catch (ex: Exception) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "Error saving location information")
            Crashlytics.logException(ex)
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
                    "Press back again to exit",
                    Toast.LENGTH_SHORT
                ).show()
                exit = true
                Handler().postDelayed({ exit = false }, (3 * 1000).toLong())
            }
        } catch (ex: Exception) {
            Crashlytics.log(
                Log.ERROR,
                LOG_TAG,
                "Error occurred while exiting Recommendations activity"
            )
            Crashlytics.logException(ex)
        }

    }

}
