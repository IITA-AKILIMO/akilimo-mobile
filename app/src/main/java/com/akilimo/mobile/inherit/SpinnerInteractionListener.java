package com.akilimo.mobile.inherit;

import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

public class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
    boolean userSelect = false;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        userSelect = true;
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (userSelect) {
            // Your selection handling code here
            userSelect = false;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
