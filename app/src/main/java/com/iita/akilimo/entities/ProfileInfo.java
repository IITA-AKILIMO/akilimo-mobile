package com.iita.akilimo.entities;

import com.iita.akilimo.utils.enums.EnumGender;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.converter.PropertyConverter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
@Entity
public class ProfileInfo {
    @Id
    long id;

    private String deviceID;
    public String userName;
    public String firstName;
    public String lastName;
    public String email;
    public String mobileCode;
    public String fullMobileNumber;
    public String farmName;
    public String fieldDescription;
    public String gender;

    public int selectedGenderIndex;

    @Getter(AccessLevel.NONE)
    public boolean sendEmail;

    @Getter(AccessLevel.NONE)
    public boolean sendSms;


    public boolean isSendEmail() {
        return this.sendEmail;
    }

    public boolean isSendSms() {
        return this.sendSms;
    }


    public String getNames() {
        return String.format("%s %s", this.firstName, this.lastName);
    }
}
