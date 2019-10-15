package com.iita.akilimo.interfaces;

import android.view.View;

public interface IRecyclerViewClickListener {
    //void onClick(View view, int position);

    void itemClicked(View view, int position, boolean isLongClick);
}
