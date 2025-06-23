package com.akilimo.mobile.exceptions

class UnknownViewModelClassException(className: String) :
    RuntimeException("Unknown ViewModel class: $className")
