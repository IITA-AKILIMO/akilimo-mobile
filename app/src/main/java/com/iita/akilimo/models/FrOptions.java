package com.iita.akilimo.models;

import com.iita.akilimo.utils.enums.EnumAdviceTasks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class FrOptions {

    @NonNull
    private String recommendationName;
    private EnumAdviceTasks recCode;
    private int image;

    public FrOptions() {

    }
}
