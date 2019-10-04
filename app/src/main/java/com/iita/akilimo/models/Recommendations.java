package com.iita.akilimo.models;

import com.iita.akilimo.utils.enums.EnumAdvice;

import lombok.Data;

@Data
public class Recommendations {

    String recommendationName;
    EnumAdvice recCode;
    int image;

    public Recommendations() {

    }
}
