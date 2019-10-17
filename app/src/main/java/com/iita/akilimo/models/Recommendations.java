package com.iita.akilimo.models;

import android.graphics.drawable.Drawable;

import com.iita.akilimo.utils.enums.EnumAdvice;

import lombok.Data;

@Data
public class Recommendations {

    private String recommendationName;
    private EnumAdvice recCode;
    private int image;
    private Drawable background;

    public Recommendations() {

    }
}
