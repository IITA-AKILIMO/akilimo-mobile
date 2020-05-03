package com.iita.akilimo.views.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.iita.akilimo.Locales;
import com.iita.akilimo.R;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.b3nedikt.app_locale.AppLocale;
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository;
import dev.b3nedikt.reword.Reword;

public class LanguagePickerActivity extends BaseActivity {

    @BindView(R.id.spinner)
    Spinner spinner;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.btnUpdateLanguage)
    AppCompatButton btnUpdateLanguage;

    private Locale selectedLocale = Locale.ENGLISH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_picker);
        ButterKnife.bind(this);

        sessionManager = new SessionManager(this);

        initToolbar();
        initComponent();
    }

    @Override
    protected void initToolbar() {
//        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_activity_language_picker);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        // toolbar.setNavigationOnClickListener(v -> closeActivity(false));
    }

    @Override
    protected void initComponent() {

        final List<String> localeStrings = new ArrayList<>();
        for (Locale locale : Locales.APP_LOCALES) {
            localeStrings.add(locale.getDisplayLanguage() + " " + locale.getCountry());
        }
        final SpinnerAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, localeStrings);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLocale = AppLocale.getSupportedLocales().get(position);
                updateSelectedLocale(selectedLocale);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnUpdateLanguage.setOnClickListener(v -> {
            Intent intent = new Intent(LanguagePickerActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
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
    }

    @Override
    protected void validate(boolean backPressed) {
    }

    private void updateToolBarTitle() {
        getSupportActionBar().setTitle(R.string.title_activity_language_picker);
    }
}
