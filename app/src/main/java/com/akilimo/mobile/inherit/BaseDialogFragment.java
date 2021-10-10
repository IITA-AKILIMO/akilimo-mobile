package com.akilimo.mobile.inherit;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.akilimo.mobile.dao.AppDatabase;
import com.akilimo.mobile.utils.MathHelper;
import com.akilimo.mobile.utils.SessionManager;

@SuppressWarnings("WeakerAccess")
public abstract class BaseDialogFragment extends DialogFragment {

    protected String LOG_TAG = BaseDialogFragment.class.getSimpleName();


    protected MathHelper mathHelper;
    protected Context context;
    protected AppDatabase database;
    protected SessionManager sessionManager;
    protected String currencySymbol;


    public BaseDialogFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mathHelper = new MathHelper();
        database = AppDatabase.getDatabase(context);
    }
}
