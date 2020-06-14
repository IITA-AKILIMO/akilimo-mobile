package com.iita.akilimo.entities;

import com.iita.akilimo.utils.enums.EnumGender;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.converter.PropertyConverter;

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

    
    public boolean sendEmail;

    
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileCode() {
        return mobileCode;
    }

    public void setMobileCode(String mobileCode) {
        this.mobileCode = mobileCode;
    }

    public String getFullMobileNumber() {
        return fullMobileNumber;
    }

    public void setFullMobileNumber(String fullMobileNumber) {
        this.fullMobileNumber = fullMobileNumber;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public String getFieldDescription() {
        return fieldDescription;
    }

    public void setFieldDescription(String fieldDescription) {
        this.fieldDescription = fieldDescription;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getSelectedGenderIndex() {
        return selectedGenderIndex;
    }

    public void setSelectedGenderIndex(int selectedGenderIndex) {
        this.selectedGenderIndex = selectedGenderIndex;
    }

    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public void setSendSms(boolean sendSms) {
        this.sendSms = sendSms;
    }
}
