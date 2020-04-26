package com.iita.akilimo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;

@Entity
public class MaizePrice extends ProducePrice {

    public MaizePrice() {

    }
}