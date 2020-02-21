package com.iita.akilimo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;


@Entity
public class InterCropFertilizer extends Fertilizer {

    public InterCropFertilizer() {

    }
}
