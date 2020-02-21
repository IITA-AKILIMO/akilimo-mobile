package com.iita.akilimo.entities;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
@Entity
public class RecAdvice {
    @Id
    long id;

    @Getter(AccessLevel.NONE)
    public boolean FR;

    @Getter(AccessLevel.NONE)
    public boolean CIM;
    @Getter(AccessLevel.NONE)
    public boolean CIS;

    @Getter(AccessLevel.NONE)
    public boolean BPP;

    @Getter(AccessLevel.NONE)
    public boolean SPH;

    @Getter(AccessLevel.NONE)
    public boolean SPP;

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
}
