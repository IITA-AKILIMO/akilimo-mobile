package com.iita.akilimo.mappers;


public class ComputedResponse {
    private String computedTitle;
    private String computedRecommendation;

    public ComputedResponse createObject(String title, String body) {
        this.setComputedTitle(title);
        this.setComputedRecommendation(body);
        return this;
    }

    public String getComputedTitle() {
        return computedTitle;
    }

    public void setComputedTitle(String computedTitle) {
        this.computedTitle = computedTitle;
    }

    public String getComputedRecommendation() {
        return computedRecommendation;
    }

    public void setComputedRecommendation(String computedRecommendation) {
        this.computedRecommendation = computedRecommendation;
    }
}
