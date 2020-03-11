package com.iita.akilimo.entities;


import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.enums.EnumAreaUnits;
import com.iita.akilimo.utils.enums.EnumCountry;
import com.iita.akilimo.utils.enums.EnumFieldArea;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.converter.PropertyConverter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
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

    @Convert(converter = CountryConverter.class, dbType = String.class)
    public EnumCountry countryEnum;
    @Convert(converter = AreaUnitConverter.class, dbType = String.class)
    public EnumAreaUnits areaUnitsEnum;

    @Convert(converter = FieldAreaConverter.class, dbType = Double.class)
    public EnumFieldArea fieldAreaEnum;

    public String areaUnit;
    public double acreAreaSize;
    public double areaSize;

    @Getter(AccessLevel.NONE)
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

}