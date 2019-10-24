package com.iita.akilimo.entities;


import com.iita.akilimo.utils.enums.EnumAreaUnits;
import com.iita.akilimo.utils.enums.EnumCountries;
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
    public double latitude;
    public double longitude;
    public double altitude;

    public String placeName;
    public String address;
    public String countryCode;
    public String countryName;
    public String currency;

    @Convert(converter = CountryConverter.class, dbType = String.class)
    public EnumCountries countryEnum;
    @Convert(converter = AreaUnitConverter.class, dbType = String.class)
    public EnumAreaUnits areaUnitsEnum;

    @Convert(converter = FieldAreaConverter.class, dbType = Double.class)
    public EnumFieldArea fieldAreaEnum;

    public String areaUnit;
    public double areaSize;

    @Getter(AccessLevel.NONE)
    public boolean exactArea;

    public boolean isExactArea() {
        return this.exactArea;
    }


    /* converter for custom data type*/
    public static class CountryConverter implements PropertyConverter<EnumCountries, String> {

        @Override
        public EnumCountries convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return EnumCountries.OTHERS;
            }
            for (EnumCountries role : EnumCountries.values()) {
                if (role.countryCode().equalsIgnoreCase(databaseValue)) {
                    return role;
                }
            }
            return EnumCountries.OTHERS;
        }

        @Override
        public String convertToDatabaseValue(EnumCountries entityProperty) {
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
}