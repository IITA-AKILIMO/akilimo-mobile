package com.iita.akilimo.entities;

import com.iita.akilimo.utils.enums.EnumCountry;
import com.iita.akilimo.utils.enums.EnumMaizeProduceType;
import com.iita.akilimo.utils.enums.EnumUnitOfSale;
import com.iita.akilimo.utils.enums.EnumUnitPrice;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.converter.PropertyConverter;

@Entity
public class MaizeMarketOutlet {
    @Id
    long id;

    private int produceRadioIndex;
    private int grainUnitRadioIndex;
    private int grainUnitPriceRadioIndex;

    private double exactPrice;
    private double averagePrice;

    @Convert(converter = ProduceTypeConverter.class, dbType = String.class)
    private EnumMaizeProduceType enumMaizeProduceType;

    @Convert(converter = UnitOfSaleConverter.class, dbType = Integer.class)
    private EnumUnitOfSale enumUnitOfSale;

    @Convert(converter = UnitPriceConverter.class, dbType = Double.class)
    private EnumUnitPrice enumUnitPrice;

    public static class ProduceTypeConverter implements PropertyConverter<EnumMaizeProduceType, String> {

        @Override
        public EnumMaizeProduceType convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return EnumMaizeProduceType.GRAIN;
            }
            for (EnumMaizeProduceType produceType : EnumMaizeProduceType.values()) {
                if (produceType.produce().equalsIgnoreCase(databaseValue)) {
                    return produceType;
                }
            }
            return EnumMaizeProduceType.GRAIN;
        }

        @Override
        public String convertToDatabaseValue(EnumMaizeProduceType entityProperty) {
            return entityProperty == null ? null : entityProperty.produce();
        }
    }

    public static class UnitPriceConverter implements PropertyConverter<EnumUnitPrice, Double> {

        @Override
        public EnumUnitPrice convertToEntityProperty(Double databaseValue) {
            if (databaseValue == null) {
                return EnumUnitPrice.UNKNOWN;
            }
            for (EnumUnitPrice produceType : EnumUnitPrice.values()) {
                if (produceType.convertToLocalCurrency(EnumCountry.OTHERS.currency(), null) == databaseValue) {
                    return produceType;
                }
            }
            return EnumUnitPrice.UNKNOWN;
        }

        @Override
        public Double convertToDatabaseValue(EnumUnitPrice entityProperty) {
            return entityProperty == null ? null : entityProperty.convertToLocalCurrency(EnumCountry.OTHERS.currency(), null);
        }
    }

    public static class UnitOfSaleConverter implements PropertyConverter<EnumUnitOfSale, Integer> {

        @Override
        public EnumUnitOfSale convertToEntityProperty(Integer databaseValue) {
            if (databaseValue == null) {
                return EnumUnitOfSale.UNIT_FIFTY_KG;
            }
            for (EnumUnitOfSale produceType : EnumUnitOfSale.values()) {
                if (produceType.unitWeight() == databaseValue) {
                    return produceType;
                }
            }
            return EnumUnitOfSale.UNIT_FIFTY_KG;
        }

        @Override
        public Integer convertToDatabaseValue(EnumUnitOfSale entityProperty) {
            return entityProperty == null ? null : entityProperty.unitWeight();
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getProduceRadioIndex() {
        return produceRadioIndex;
    }

    public void setProduceRadioIndex(int produceRadioIndex) {
        this.produceRadioIndex = produceRadioIndex;
    }

    public int getGrainUnitRadioIndex() {
        return grainUnitRadioIndex;
    }

    public void setGrainUnitRadioIndex(int grainUnitRadioIndex) {
        this.grainUnitRadioIndex = grainUnitRadioIndex;
    }

    public int getGrainUnitPriceRadioIndex() {
        return grainUnitPriceRadioIndex;
    }

    public void setGrainUnitPriceRadioIndex(int grainUnitPriceRadioIndex) {
        this.grainUnitPriceRadioIndex = grainUnitPriceRadioIndex;
    }

    public double getExactPrice() {
        return exactPrice;
    }

    public void setExactPrice(double exactPrice) {
        this.exactPrice = exactPrice;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(double averagePrice) {
        this.averagePrice = averagePrice;
    }

    public EnumMaizeProduceType getEnumMaizeProduceType() {
        return enumMaizeProduceType;
    }

    public void setEnumMaizeProduceType(EnumMaizeProduceType enumMaizeProduceType) {
        this.enumMaizeProduceType = enumMaizeProduceType;
    }

    public EnumUnitOfSale getEnumUnitOfSale() {
        return enumUnitOfSale;
    }

    public void setEnumUnitOfSale(EnumUnitOfSale enumUnitOfSale) {
        this.enumUnitOfSale = enumUnitOfSale;
    }

    public EnumUnitPrice getEnumUnitPrice() {
        return enumUnitPrice;
    }

    public void setEnumUnitPrice(EnumUnitPrice enumUnitPrice) {
        this.enumUnitPrice = enumUnitPrice;
    }
}
