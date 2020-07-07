package com.iita.akilimo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.iita.akilimo.R;

import java.util.List;

public class MySpinnerAdapter extends BaseAdapter {
    private Context context;
    private List<String> spinnerItems;
    private List<Integer> spinnerImages;
    private LayoutInflater layoutInflater;

    public MySpinnerAdapter(Context applicationContext, List<String> localeStrings, List<Integer> countryStrings) {
        this.context = applicationContext;
        this.spinnerItems = localeStrings;
        this.spinnerImages = countryStrings;
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

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = layoutInflater.inflate(R.layout.custom_spinner, null);
        ImageView icon = (ImageView) view.findViewById(R.id.spinnerImage);
        TextView names = (TextView) view.findViewById(R.id.spinnerText);
        try {
            icon.setImageResource(spinnerImages.get(position));
            names.setText(spinnerItems.get(position));
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, "SPINNER_ADAPTER", ex.getMessage());
            Crashlytics.logException(ex);
        }
        return view;
    }
}
