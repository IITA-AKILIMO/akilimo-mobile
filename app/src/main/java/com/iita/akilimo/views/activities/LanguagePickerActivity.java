package com.iita.akilimo.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.blongho.country_data.World;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.iita.akilimo.Locales;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.MySpinnerAdapter;
import com.iita.akilimo.databinding.ActivityLanguagePickerBinding;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.SessionManager;
import com.iita.akilimo.utils.enums.EnumCountry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.b3nedikt.app_locale.AppLocale;
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository;
import dev.b3nedikt.reword.Reword;

public class LanguagePickerActivity extends BaseActivity {


    private Spinner languageSpinner;
    private Toolbar toolbar;
    private TextView versionInfo;
    private AppCompatButton btnUpdateLanguage;

    ActivityLanguagePickerBinding binding;

    private Locale selectedLocale = Locale.ENGLISH;

    private int selectedLanguageIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_language_picker);
        binding = ActivityLanguagePickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        toolbar = binding.toolbarLayout.toolbar;
        versionInfo = binding.contentLanguage.versionInfo;
        languageSpinner = binding.contentLanguage.languageSpinner;
        btnUpdateLanguage = binding.contentLanguage.btnUpdateLanguage;


        initToolbar();
        initComponent();
    }

    @Override
    protected void initToolbar() {
        try {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.title_activity_language_picker);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        } catch (Exception ex) {
            FirebaseCrashlytics.getInstance().log(ex.getMessage());
            FirebaseCrashlytics.getInstance().recordException(ex);
        }
    }

    @Override
    protected void initComponent() {

        final List<String> localeStrings = new ArrayList<>();
        final List<Integer> countryImages = new ArrayList<>();
        for (Locale locale : Locales.APP_LOCALES) {
            String languageCountry = locale.getCountry();
            if (languageCountry.equalsIgnoreCase(EnumCountry.Tanzania.countryCode())) {
                localeStrings.add(getString(R.string.lbl_kiswahili));
            } else {
                localeStrings.add(locale.getDisplayLanguage());
            }
            final int flag = World.getFlagOf(languageCountry);
            countryImages.add(flag);
        }
        final MySpinnerAdapter spinnerAdapter = new MySpinnerAdapter(this, localeStrings, countryImages);

        languageSpinner.setAdapter(spinnerAdapter);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLanguageIndex = position;
                selectedLocale = AppLocale.getSupportedLocales().get(selectedLanguageIndex);
                updateSelectedLocale(selectedLocale);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnUpdateLanguage.setOnClickListener(v -> {
            Intent intent = new Intent(LanguagePickerActivity.this, HomeActivity.class);
            startActivity(intent);
            closeActivity(true);
        });
    }

    private void updateSelectedLocale(Locale selectedLocale) {
        AppLocale.setDesiredLocale(selectedLocale);
        SharedPrefsAppLocaleRepository prefs = new SharedPrefsAppLocaleRepository(LanguagePickerActivity.this);
        prefs.setDesiredLocale(selectedLocale);
        AppLocale.setAppLocaleRepository(prefs); //persist changes

        AppLocale.setDesiredLocale(selectedLocale);
        // The layout containing the views you want to localize
        final View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        Reword.reword(rootView);
        updateToolBarTitle();
        languageSpinner.setSelection(selectedLanguageIndex);
    }

    @Override
    protected void validate(boolean backPressed) {
    }

    private void updateToolBarTitle() {
        getSupportActionBar().setTitle(R.string.title_activity_language_picker);
        versionInfo.setText(sessionManager.getAppVersion());
    }
}
