package com.iita.akilimo.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.iita.akilimo.Locales;
import com.iita.akilimo.R;
import com.iita.akilimo.inherit.BaseFragment;

import org.modelmapper.internal.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import dev.b3nedikt.app_locale.AppLocale;
import dev.b3nedikt.reword.Reword;

/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link LanguagePickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LanguagePickerFragment extends BaseFragment {

    @BindView(R.id.spinner)
    Spinner spinner;

    public LanguagePickerFragment() {
        // Required empty public constructor
    }

    public static LanguagePickerFragment newInstance() {
        return new LanguagePickerFragment();
    }

    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_language_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final List<String> localeStrings = new ArrayList<>();
        for (Locale locale : Locales.APP_LOCALES) {
            localeStrings.add(locale.getDisplayLanguage() + " " + locale.getCountry());
        }

        final SpinnerAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, localeStrings);
        spinner.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppLocale.setDesiredLocale(AppLocale.getSupportedLocales().get(position));
                
                // The layout containing the views you want to localize
//                final View rootView = view.findViewById(android.R.id.content);
//                Reword.reword(rootView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void refreshData() {

    }

}
