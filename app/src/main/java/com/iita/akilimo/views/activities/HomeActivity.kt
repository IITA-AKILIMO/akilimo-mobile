package com.iita.akilimo.views.activities


import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
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
import com.google.android.gms.common.util.Strings
import com.iita.akilimo.R
import com.iita.akilimo.adapters.ViewPagerAdapter
import com.iita.akilimo.entities.MandatoryInfo
import com.iita.akilimo.inherit.BaseActivity
import com.iita.akilimo.interfaces.IFragmentCallBack
import com.iita.akilimo.utils.Tools
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor
import com.iita.akilimo.views.fragments.*
import timber.log.Timber


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
    private var viewPager: ViewPager? = null
    private var myViewPagerAdapter: ViewPagerAdapter? = null

    var exit: Boolean = false
    var showProceedButton: Boolean = true
    var currentLat: Double = 0.toDouble()
    var currentLong: Double = 0.toDouble()
    var currentAlt: Double = 0.toDouble()
    var placeName: String? = null
    var address: String? = null
    var location: MandatoryInfo? = null

    private var activity: Activity? = null
    var btnStart: Button? = null


    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is SummaryFragment) {
            fragment.setOnFragmentCloseListener(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        ButterKnife.bind(this)
        activity = this
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(this)

        viewPager = findViewById<ViewPager>(R.id.homeViewPager)
        btnStart = findViewById<Button>(R.id.btnGetStarted)

        //Add the various fragments
        fragmentArray.add(WelcomeFragment.newInstance())
        fragmentArray.add(CountryFragment.newInstance())
        fragmentArray.add(LocationFragment.newInstance())
        fragmentArray.add(AreaUnitFragment.newInstance())
        fragmentArray.add(FieldSizeFragment.newInstance())
        fragmentArray.add(SummaryFragment.newInstance())


        //add bottom progress dots
        bottomProgressDots(0)

        myViewPagerAdapter = ViewPagerAdapter(supportFragmentManager, 0, fragmentArray)

        viewPager?.adapter = myViewPagerAdapter
        viewPager?.offscreenPageLimit = 1

        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                val fragment: Fragment = fragmentArray.elementAt(position)
                (fragment as? CountryFragment)?.refreshData()

                (fragment as? AreaUnitFragment)?.refreshData()

                (fragment as? FieldSizeFragment)?.refreshData()

                (fragment as? LocationFragment)?.refreshData()

                (fragment as? SummaryFragment)?.refreshData()
            }

            override fun onPageSelected(position: Int) {
                bottomProgressDots(position)
                Tools.setSystemBarColor(activity, R.color.deep_orange_500)
                when (position) {
                    fragmentArray.size - 1 -> {
                        if (showProceedButton) {
                            btnStart?.visibility = View.GONE
                        }
                        Tools.setSystemBarColor(activity, R.color.blue_400)
                    }
                    else -> {
                        btnStart?.visibility = View.GONE
                    }
                }
            }

        })

        btnStart?.setOnClickListener {
            val intent = Intent(this, RecommendationsActivity::class.java)
            startActivity(intent)
            Animatoo.animateShrink(this)
        }
        initComponent()
    }

    override fun onFragmentClose(hideButton: Boolean) {
        showProceedButton = hideButton
        when {
            !hideButton -> {
                btnStart?.visibility = View.VISIBLE
                btnStart?.text = getString(R.string.lbl_proceed)
            }
            else -> btnStart?.visibility = View.GONE
        }
    }

    override fun initToolbar() {
        throw UnsupportedOperationException()
    }

    override fun validate(backPressed: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun initComponent() {
        Tools.setSystemBarColor(activity, R.color.deep_orange_500)
        checkAppPermissions(rationale)
        btnStart?.visibility = View.GONE
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
                    R.color.deep_orange_500
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
            if (location == null) {
                location = MandatoryInfo()
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

            objectBoxEntityProcessor.saveMandatoryInfo(location)
        } catch (ex: Exception) {
            Timber.e(ex)
        }

    }

}
