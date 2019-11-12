package com.iita.akilimo.entities;

import com.iita.akilimo.utils.enums.EnumCountries;
import com.iita.akilimo.utils.enums.EnumProduceType;
import com.iita.akilimo.utils.enums.EnumUnitOfSale;
import com.iita.akilimo.utils.enums.EnumUnitPrice;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.converter.PropertyConverter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
@Entity
public class MarketOutlet {
    @Id
    long id;

    private String starchFactory;
    private double exactPrice;

    @Getter(AccessLevel.NONE)
    public boolean starchFactoryRequired;

    public boolean isStarchFactoryRequired() {
        return this.starchFactoryRequired;
    }

    @Convert(converter = ProduceTypeConverter.class, dbType = String.class)
    public EnumProduceType enumProduceType;

    @Convert(converter = UnitOfSaleConverter.class, dbType = Integer.class)
    public EnumUnitOfSale enumUnitOfSale;

    @Convert(converter = UnitPriceConverter.class, dbType = Double.class)
    public EnumUnitPrice enumUnitPrice;

    public static class ProduceTypeConverter implements PropertyConverter<EnumProduceType, String> {

        @Override
        public EnumProduceType convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return EnumProduceType.UNKNOWN;
            }
            for (EnumProduceType produceType : EnumProduceType.values()) {
                if (produceType.produce().equalsIgnoreCase(databaseValue)) {
                    return produceType;
                }
            }
            return EnumProduceType.UNKNOWN;
        }

        @Override
        public String convertToDatabaseValue(EnumProduceType entityProperty) {
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
                if (produceType.convertToLocal(EnumCountries.OTHERS.currency()) == databaseValue) {
                    return produceType;
                }
            }
            return EnumUnitPrice.UNKNOWN;
        }

        @Override
        public Double convertToDatabaseValue(EnumUnitPrice entityProperty) {
            return entityProperty == null ? null : entityProperty.convertToLocal(EnumCountries.OTHERS.currency());
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
}
