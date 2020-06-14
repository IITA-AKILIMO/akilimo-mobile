package com.iita.akilimo.models;

import androidx.annotation.NonNull;

import com.iita.akilimo.utils.enums.EnumAdviceTasks;


public class RecommendationOptions {

    private String recommendationName;
    private EnumAdviceTasks recCode;
    private int image;

    public RecommendationOptions() {
    }

    public RecommendationOptions(String recName, EnumAdviceTasks recommendationCode, int imageId) {
        this.setRecommendationName(recName);
        this.setRecCode(recommendationCode);
        this.setImage(imageId);
    }

    public String getRecommendationName() {
        return recommendationName;
    }

    public void setRecommendationName(@NonNull String recommendationName) {
        this.recommendationName = recommendationName;
    }

    public EnumAdviceTasks getRecCode() {
        return recCode;
    }

    public void setRecCode(EnumAdviceTasks recCode) {
        this.recCode = recCode;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
