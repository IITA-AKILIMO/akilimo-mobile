package com.iita.akilimo.views.activities.usecases;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.BuildConfig;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.AdapterListAnimation;
import com.iita.akilimo.dao.AppDatabase;
import com.iita.akilimo.databinding.ActivityRecommendationsActivityBinding;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.models.Recommendations;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.SessionManager;
import com.iita.akilimo.utils.enums.EnumAdvice;
import com.iita.akilimo.utils.enums.EnumCountry;

import java.util.ArrayList;
import java.util.List;

public class RecommendationsActivity extends BaseActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    UnifiedNativeAd nativeAd;

    ActivityRecommendationsActivityBinding binding;


    String frString;
    String icMaizeString;
    String icSweetPotatoString;
    String sphString;
    String bppString;


    private MandatoryInfo mandatoryInfo;
    private AdapterListAnimation mAdapter;
    private List<Recommendations> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecommendationsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;
        database = AppDatabase.getDatabase(context);
        if (sessionManager == null) {
            sessionManager = new SessionManager(context);
        }
        ProfileInfo profileInfo = database.profileInfoDao().findOne();
        if (profileInfo != null) {
            countryCode = profileInfo.getCountryCode();
            currency = profileInfo.getCurrency();
        }

        toolbar = binding.toolbarLayout.toolbar;
        recyclerView = binding.recyclerView;
        initToolbar();
        initComponent();

        refreshAd();
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_home);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.lbl_recommendations));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> closeActivity(false));
    }


    @Override
    protected void initComponent() {
        frString = getString(R.string.lbl_fertilizer_recommendations);
        icMaizeString = getString(R.string.lbl_intercropping_maize);
        icSweetPotatoString = getString(R.string.lbl_intercropping_sweet_potato);
        sphString = getString(R.string.lbl_scheduled_planting_and_harvest);
        bppString = getString(R.string.lbl_best_planting_practices);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        //set data and list adapter
        mAdapter = new AdapterListAnimation(this, R.layout.item_card_recommendation_no_arrow);
        recyclerView.setAdapter(mAdapter);
        items = new ArrayList<>();

        Recommendations FR = new Recommendations();
        FR.setRecCode(EnumAdvice.FR);
        FR.setRecommendationName(frString);
        FR.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_gradient_very_soft));
        items.add(FR);

        if (countryCode.equals(EnumCountry.Nigeria.countryCode())) {
            Recommendations IC_MAIZE = new Recommendations();
            IC_MAIZE.setRecCode(EnumAdvice.IC_MAIZE);
            IC_MAIZE.setRecommendationName(icMaizeString);
            IC_MAIZE.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_gradient_very_soft));
            items.add(IC_MAIZE);
        } else if (countryCode.equals(EnumCountry.Tanzania.countryCode())) {
            Recommendations IC_SWEET_POTATO = new Recommendations();
            IC_SWEET_POTATO.setRecCode(EnumAdvice.IC_SWEET_POTATO);
            IC_SWEET_POTATO.setRecommendationName(icSweetPotatoString);
            IC_SWEET_POTATO.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_gradient_very_soft));
            items.add(IC_SWEET_POTATO);
        }

        Recommendations SPH = new Recommendations();
        SPH.setRecCode(EnumAdvice.SPH);
        SPH.setRecommendationName(sphString);
        SPH.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_gradient_very_soft));
        items.add(SPH);

        Recommendations BPP = new Recommendations();
        BPP.setRecCode(EnumAdvice.BPP);
        BPP.setRecommendationName(bppString);
        BPP.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_gradient_very_soft));
        items.add(BPP);


        setAdapter();
    }

    @Override
    protected void validate(boolean backPressed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return true;
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(context, R.string.lbl_back_instructions, Toast.LENGTH_SHORT)
                .show();
    }

    private void setAdapter() {
        mAdapter.setItems(items, ItemAnimation.BOTTOM_UP);
        // on item list clicked
        mAdapter.setOnItemClickListener((view, obj, position) -> {
            //let us process the data
            Intent intent = null;
            EnumAdvice advice = obj.getRecCode();
            if (advice == null) {
                advice = EnumAdvice.WM;
            }
            switch (advice) {
                case FR:
                    intent = new Intent(this, FertilizerRecActivity.class);
                    break;
                case BPP:
                    intent = new Intent(this, PlantingPracticesActivity.class);
                    break;
                case IC_MAIZE:
                case IC_SWEET_POTATO:
                    intent = new Intent(this, InterCropRecActivity.class);
                    break;
                case SPH:
                    intent = new Intent(this, ScheduledPlantingActivity.class);
                    break;
                case WM:
                    break;
            }
            if (intent != null) {
                startActivity(intent);
                openActivity();
            } else {
                Snackbar.make(view, "Item " + obj.getRecommendationName() + " clicked but not launched", Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    private void refreshAd() {
        boolean showAd = sessionManager.showAd();
        if (!showAd) {
            Toast.makeText(RecommendationsActivity.this, "Processing collected info for future targeting", Toast.LENGTH_SHORT).show();
            return;
        }
        String adUnit = "ca-app-pub-7182284303548130/5991780514";
        if (BuildConfig.DEBUG) {
            adUnit = "ca-app-pub-3940256099942544/2247696110";
        }
        AdLoader.Builder builder = new AdLoader.Builder(this, adUnit);

        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            // OnUnifiedNativeAdLoadedListener implementation.
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                // You must call destroy on old ads when you are done with them,
                // otherwise you will have a memory leak.
                if (nativeAd != null) {
                    nativeAd.destroy();
                }
                nativeAd = unifiedNativeAd;
                FrameLayout frameLayout =
                        findViewById(R.id.fl_adplaceholder);
                UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater()
                        .inflate(R.layout.ad_unified, null);
                populateUnifiedNativeAdView(unifiedNativeAd, adView);
                frameLayout.removeAllViews();
                frameLayout.addView(adView);
            }

        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Toast.makeText(RecommendationsActivity.this, "Failed to load native ad: "
                        + errorCode, Toast.LENGTH_SHORT).show();
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    /**
     * Populates a {@link UnifiedNativeAdView} object with data from a given
     * {@link UnifiedNativeAd}.
     *
     * @param nativeAd the object containing the ad's assets
     * @param adView   the view to be populated
     */
    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        // Set the media view.
        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);
    }

    @Override
    protected void onDestroy() {
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        super.onDestroy();
    }
}
