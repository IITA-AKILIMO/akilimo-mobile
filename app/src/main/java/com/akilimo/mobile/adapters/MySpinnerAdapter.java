package com.akilimo.mobile.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.akilimo.mobile.R;

import java.util.List;

import io.sentry.Sentry;

public class MySpinnerAdapter extends BaseAdapter {
    private Context context;
    private List<String> spinnerItems;
    private List<Integer> spinnerImages;
    private LayoutInflater layoutInflater;

    @Deprecated
    public MySpinnerAdapter(Context applicationContext, List<String> localeStrings, List<Integer> countryStrings) {
        this.context = applicationContext;
        this.spinnerItems = localeStrings;
        this.spinnerImages = countryStrings;
        layoutInflater = (LayoutInflater.from(applicationContext));
    }

    public MySpinnerAdapter(Context applicationContext, List<String> localeStrings) {
        this.context = applicationContext;
        this.spinnerItems = localeStrings;
        layoutInflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return spinnerItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = layoutInflater.inflate(R.layout.custom_spinner, null);
        TextView names = (TextView) view.findViewById(R.id.spinnerText);
        try {
            names.setText(spinnerItems.get(position));
        } catch (Exception ex) {
            Sentry.captureException(ex);
        }
        return view;
    }
}
