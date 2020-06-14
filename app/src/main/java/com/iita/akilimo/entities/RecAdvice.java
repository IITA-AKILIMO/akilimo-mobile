package com.iita.akilimo.entities;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class RecAdvice {
    @Id
    long id;


    public boolean FR;


    public boolean CIM;


    public boolean CIS;


    public boolean BPP;


    public boolean SPH;


    public boolean SPP;

    public String useCase;

    public boolean isFR() {
        return this.FR;
    }

    public boolean isCIM() {
        return this.CIM;
    }

    public boolean isCIS() {
        return this.CIS;
    }

    public boolean isBPP() {
        return this.BPP;
    }

    public boolean isSPH() {
        return this.SPH;
    }

    public boolean isSPP() {
        return this.SPP;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setFR(boolean FR) {
        this.FR = FR;
    }

    public void setCIM(boolean CIM) {
        this.CIM = CIM;
    }

    public void setCIS(boolean CIS) {
        this.CIS = CIS;
    }

    public void setBPP(boolean BPP) {
        this.BPP = BPP;
    }

    public void setSPH(boolean SPH) {
        this.SPH = SPH;
    }

    public void setSPP(boolean SPP) {
        this.SPP = SPP;
    }

    public String getUseCase() {
        return useCase;
    }

    public void setUseCase(String useCase) {
        this.useCase = useCase;
    }
}
