package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.Locales;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.MySpinnerAdapter;
import com.iita.akilimo.databinding.FragmentWelcomeBinding;
import com.iita.akilimo.inherit.BaseStepFragment;
import com.iita.akilimo.interfaces.IFragmentCallBack;
import com.iita.akilimo.utils.enums.EnumCountry;
import com.iita.akilimo.views.activities.HomeStepperActivity;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.stepstone.stepper.VerificationError;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.b3nedikt.app_locale.AppLocale;
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository;
import dev.b3nedikt.reword.Reword;

/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeFragment extends BaseStepFragment {

    private FragmentWelcomeBinding binding;
    private IFragmentCallBack fragmentCallBack;
    private Spinner languagePicker;
    private SharedPrefsAppLocaleRepository prefs;
    private int selectedLanguageIndex = -1;
    private Locale selectedLocale;
    private boolean languagePicked = false;

    LinearLayout layout;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }


    public static WelcomeFragment newInstance() {
        return new WelcomeFragment();
    }


    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        languagePicker = binding.languagePicker;
        layout = binding.welcomeLayout;
        prefs = new SharedPrefsAppLocaleRepository(context);

        languagePicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                languagePicked = true;
                return false;
            }
        });
        languagePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (languagePicked) {
                    selectedLanguageIndex = position;
                    selectedLocale = AppLocale.getSupportedLocales().get(selectedLanguageIndex);

                    AppLocale.setDesiredLocale(selectedLocale);
                    SharedPrefsAppLocaleRepository prefs = new SharedPrefsAppLocaleRepository(context);
                    prefs.setDesiredLocale(selectedLocale);
                    AppLocale.setAppLocaleRepository(prefs); //persist changes

                    AppLocale.setDesiredLocale(selectedLocale);

                    final View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
                    Reword.reword(rootView);

                    Intent intent = new Intent(context, HomeStepperActivity.class);
                    Snackbar snackBar = Snackbar
                            .make(layout, getString(R.string.lbl_restart_app_prompt), Snackbar.LENGTH_INDEFINITE)
                            .setAction(context.getString(R.string.lbl_ok), view1 -> ProcessPhoenix.triggerRebirth(context, intent));
                    snackBar.show();
                    initSpinnerItems();
                }
                languagePicked = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        initSpinnerItems();
    }

    private void initSpinnerItems() {
        selectedLocale = prefs.getDesiredLocale();

        final List<String> localeStrings = new ArrayList<>();
        final List<String> localeDisplayName = new ArrayList<>();
        for (Locale locale : Locales.APP_LOCALES) {
            String languageCountry = locale.getCountry();
            localeDisplayName.add(locale.getDisplayLanguage());
            if (languageCountry.equalsIgnoreCase(EnumCountry.Tanzania.countryCode())) {
                localeStrings.add(getString(R.string.lbl_kiswahili));
            } else {
                localeStrings.add(locale.getDisplayLanguage());
            }
        }
        final MySpinnerAdapter spinnerAdapter = new MySpinnerAdapter(context, localeStrings);
        languagePicker.setAdapter(spinnerAdapter);

        if (selectedLocale != null) {
            selectedLanguageIndex = localeDisplayName.indexOf(selectedLocale.getDisplayLanguage());
        }
        languagePicker.setSelection(selectedLanguageIndex);
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        return verificationError;
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {
    }

    public void setOnFragmentCloseListener(IFragmentCallBack callBack) {
        this.fragmentCallBack = callBack;
    }

    private void reloadView() {
        if (fragmentCallBack != null) {
            fragmentCallBack.reloadView();
        }
    }
}
