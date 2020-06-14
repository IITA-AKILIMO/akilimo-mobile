package com.iita.akilimo.entities;


import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.enums.EnumAreaUnits;
import com.iita.akilimo.utils.enums.EnumCountry;
import com.iita.akilimo.utils.enums.EnumFieldArea;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.converter.PropertyConverter;


@Entity
public class MandatoryInfo {

    @Id
    long id;

    private String placeName;
    private String address;
    private String countryCode;
    private String countryName;
    private String currency;
    private int fieldSizeRadioIndex;
    private int selectedCountryIndex;

    @Convert(converter = CountryConverter.class, dbType = String.class)
    public EnumCountry countryEnum;
    @Convert(converter = AreaUnitConverter.class, dbType = String.class)
    public EnumAreaUnits areaUnitsEnum;

    @Convert(converter = FieldAreaConverter.class, dbType = Double.class)
    public EnumFieldArea fieldAreaEnum;

    public String areaUnit;
    public double acreAreaSize;
    public double areaSize;

    public boolean exactArea;

    public boolean isExactArea() {
        return this.exactArea;
    }

    /* converter for custom data type*/
    public static class CountryConverter implements PropertyConverter<EnumCountry, String> {

        @Override
        public EnumCountry convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return EnumCountry.OTHERS;
            }
            for (EnumCountry role : EnumCountry.values()) {
                if (role.countryCode().equalsIgnoreCase(databaseValue)) {
                    return role;
                }
            }
            return EnumCountry.OTHERS;
        }

        @Override
        public String convertToDatabaseValue(EnumCountry entityProperty) {
            return entityProperty == null ? null : entityProperty.countryCode();
        }
    }

    public static class AreaUnitConverter implements PropertyConverter<EnumAreaUnits, String> {

        @Override
        public EnumAreaUnits convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return EnumAreaUnits.UNKNOWN;
            }
            for (EnumAreaUnits role : EnumAreaUnits.values()) {
                if (role.unitString().equalsIgnoreCase(databaseValue)) {
                    return role;
                }
            }
            return EnumAreaUnits.ACRE;
        }

        @Override
        public String convertToDatabaseValue(EnumAreaUnits entityProperty) {
            return entityProperty == null ? null : entityProperty.unitString();
        }
    }

    public static class FieldAreaConverter implements PropertyConverter<EnumFieldArea, Double> {

        @Override
        public EnumFieldArea convertToEntityProperty(Double databaseValue) {
            if (databaseValue == null) {
                return EnumFieldArea.UNKNOWN;
            }
            for (EnumFieldArea role : EnumFieldArea.values()) {
                if (role.areaValue() == databaseValue) {
                    return role;
                }
            }
            return EnumFieldArea.ONE_ACRE;
        }

        @Override
        public Double convertToDatabaseValue(EnumFieldArea entityProperty) {
            return entityProperty == null ? null : entityProperty.areaValue();
        }
    }

    public void convertToSelectedAreaUnit() {
        MathHelper mathHelper = new MathHelper();
        double convertedArea = 0.0;
        double nearestValue = 1000.0;
        if (this.getAreaUnitsEnum() != null) {
            switch (this.getAreaUnitsEnum()) {
                case ACRE:
                    convertedArea = this.acreAreaSize;
                    break;
                case HA:
                    convertedArea = this.acreAreaSize / 2.471;
                    break;
                case SQM:
                    convertedArea = this.acreAreaSize * 4046.856;
                    break;
            }
            this.setAreaSize(mathHelper.roundToNearestSpecifiedValue(convertedArea, nearestValue));
        }
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getFieldSizeRadioIndex() {
        return fieldSizeRadioIndex;
    }

    public void setFieldSizeRadioIndex(int fieldSizeRadioIndex) {
        this.fieldSizeRadioIndex = fieldSizeRadioIndex;
    }

    public int getSelectedCountryIndex() {
        return selectedCountryIndex;
    }

    public void setSelectedCountryIndex(int selectedCountryIndex) {
        this.selectedCountryIndex = selectedCountryIndex;
    }

    public EnumCountry getCountryEnum() {
        return countryEnum;
    }

    public void setCountryEnum(EnumCountry countryEnum) {
        this.countryEnum = countryEnum;
    }

    public EnumAreaUnits getAreaUnitsEnum() {
        return areaUnitsEnum;
    }

    public void setAreaUnitsEnum(EnumAreaUnits areaUnitsEnum) {
        this.areaUnitsEnum = areaUnitsEnum;
    }

    public EnumFieldArea getFieldAreaEnum() {
        return fieldAreaEnum;
    }

    public void setFieldAreaEnum(EnumFieldArea fieldAreaEnum) {
        this.fieldAreaEnum = fieldAreaEnum;
    }

    public String getAreaUnit() {
        return areaUnit;
    }

    public void setAreaUnit(String areaUnit) {
        this.areaUnit = areaUnit;
    }

    public double getAcreAreaSize() {
        return acreAreaSize;
    }

    public void setAcreAreaSize(double acreAreaSize) {
        this.acreAreaSize = acreAreaSize;
    }

    public double getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(double areaSize) {
        this.areaSize = areaSize;
    }

    public void setExactArea(boolean exactArea) {
        this.exactArea = exactArea;
    }
}