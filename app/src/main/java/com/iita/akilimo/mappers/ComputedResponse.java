package com.iita.akilimo.mappers;

import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class ComputedResponse {
    private String computedTitle;
    private String computedRecommendation;

    public ComputedResponse createObject(String title, String body) {
        this.setComputedTitle(title);
        this.setComputedRecommendation(body);
        return this;
    }
}
