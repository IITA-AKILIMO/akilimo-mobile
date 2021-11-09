package com.akilimo.mobile.interfaces

import android.view.View

interface IRecyclerViewClickListener {
    //void onClick(View view, int position);

    fun itemClicked(view: View, position: Int, isLongClick: Boolean)
}
