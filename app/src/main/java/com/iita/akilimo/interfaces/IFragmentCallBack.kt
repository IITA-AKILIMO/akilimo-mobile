package com.iita.akilimo.interfaces

interface IFragmentCallBack {
    fun onFragmentClose(hideButton: Boolean)

    fun onDataSaved()

    fun sendResult(requestCode: Int, obj: Any)
}
