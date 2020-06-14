package com.iita.akilimo.entities;

import com.iita.akilimo.utils.enums.EnumCassavaProduceType;
import com.iita.akilimo.utils.enums.EnumCountry;
import com.iita.akilimo.utils.enums.EnumUnitOfSale;
import com.iita.akilimo.utils.enums.EnumUnitPrice;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.converter.PropertyConverter;

@Entity
public class CassavaMarketOutlet {
    @Id
    long id;

    private String starchFactory;
    private double exactPrice;
    private double averagePrice;

    public boolean starchFactoryRequired;

    public boolean isStarchFactoryRequired() {
        return this.starchFactoryRequired;
    }

    @Convert(converter = ProduceTypeConverter.class, dbType = String.class)
    public EnumCassavaProduceType enumCassavaProduceType;

    @Convert(converter = UnitOfSaleConverter.class, dbType = Integer.class)
    public EnumUnitOfSale enumUnitOfSale;

    @Convert(converter = UnitPriceConverter.class, dbType = Double.class)
    public EnumUnitPrice enumUnitPrice;

    public static class ProduceTypeConverter implements PropertyConverter<EnumCassavaProduceType, String> {

        @Override
        public EnumCassavaProduceType convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return EnumCassavaProduceType.ROOTS;
            }
            for (EnumCassavaProduceType produceType : EnumCassavaProduceType.values()) {
                if (produceType.produce().equalsIgnoreCase(databaseValue)) {
                    return produceType;
                }
            }
            return EnumCassavaProduceType.ROOTS;
        }

        @Override
        public String convertToDatabaseValue(EnumCassavaProduceType entityProperty) {
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

    public String getStarchFactory() {
        return starchFactory;
    }

    public void setStarchFactory(String starchFactory) {
        this.starchFactory = starchFactory;
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

    public void setStarchFactoryRequired(boolean starchFactoryRequired) {
        this.starchFactoryRequired = starchFactoryRequired;
    }

    public EnumCassavaProduceType getEnumCassavaProduceType() {
        return enumCassavaProduceType;
    }

    public void setEnumCassavaProduceType(EnumCassavaProduceType enumCassavaProduceType) {
        this.enumCassavaProduceType = enumCassavaProduceType;
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
