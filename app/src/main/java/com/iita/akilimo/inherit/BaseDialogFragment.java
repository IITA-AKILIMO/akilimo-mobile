package com.iita.akilimo.inherit;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.iita.akilimo.dao.AppDatabase;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.SessionManager;

@SuppressWarnings("WeakerAccess")
public abstract class BaseDialogFragment extends DialogFragment {

    protected String LOG_TAG = BaseDialogFragment.class.getSimpleName();


    protected MathHelper mathHelper;
    protected Context context;
    protected AppDatabase database;
    protected SessionManager sessionManager;


    public BaseDialogFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mathHelper = new MathHelper();
        database = AppDatabase.getDatabase(context);
    }
}
