package com.iita.akilimo.inherit;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.iita.akilimo.Akilimo;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.LocationInfo;
import com.iita.akilimo.utils.SessionManager;
import com.iita.akilimo.utils.objectbox.ObjectBox;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;

import butterknife.BindString;
import butterknife.ButterKnife;
import dev.b3nedikt.reword.Reword;
import io.objectbox.BoxStore;

@SuppressWarnings("WeakerAccess")
public abstract class BaseFragment extends Fragment {

    protected String TAG = BaseFragment.class.getSimpleName();

    protected int nextTab = 0;
    protected int prevTab = 0;
    protected double baseAcre = 2.471;

    protected String currency;
    protected String countryCode;
    protected String countryName;


    @BindString(R.string.empty_text)
    String emptyText;

    private String appVersion;
    protected Context context;
    protected BoxStore boxStore;
    protected RequestQueue queue;
    protected ObjectBoxEntityProcessor objectBoxEntityProcessor;
    protected SessionManager sessionManager;

    public BaseFragment() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        sessionManager = new SessionManager(getContext());
        boxStore = ObjectBox.get();
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(getActivity());
        queue = Volley.newRequestQueue(context.getApplicationContext());
        appVersion = sessionManager.getAppVersion();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = loadFragmentLayout(inflater, container, savedInstanceState);

        Reword.reword(view);
        ButterKnife.bind(this, view);

        return view;
    }

    protected abstract View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    public abstract void refreshData();

    protected StringBuilder loadLocationInfo(LocationInfo locationInfo) {
        StringBuilder stBuilder = new StringBuilder();
        if (locationInfo != null) {
            String latitude = String.valueOf(locationInfo.getLatitude());
            String longitude = String.valueOf(locationInfo.getLongitude());
            stBuilder.append("Lat:");
            stBuilder.append(latitude);
            stBuilder.append(" ");
            stBuilder.append("Lon:");
            stBuilder.append(longitude);
        }

        return stBuilder;
    }
}
