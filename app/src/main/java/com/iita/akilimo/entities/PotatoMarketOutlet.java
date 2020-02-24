package com.iita.akilimo.entities;

import com.iita.akilimo.utils.enums.EnumCountries;
import com.iita.akilimo.utils.enums.EnumPotatoProduceType;
import com.iita.akilimo.utils.enums.EnumPotatoUnitPrice;
import com.iita.akilimo.utils.enums.EnumUnitOfSale;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.converter.PropertyConverter;
import lombok.Data;

@Data
@Entity
public class PotatoMarketOutlet {
    @Id
    long id;

    private int produceTypeRadioIndex;
    private int potatoUnitOfSaleRadioIndex;
    private int potatoUnitPriceRadioIndex;
    private double exactPrice;

    @Convert(converter = ProduceTypeConverter.class, dbType = String.class)
    private EnumPotatoProduceType enumPotatoProduceType;

    @Convert(converter = UnitOfSaleConverter.class, dbType = Integer.class)
    private EnumUnitOfSale enumUnitOfSale;

    @Convert(converter = UnitPriceConverter.class, dbType = Double.class)
    private EnumPotatoUnitPrice enumPotatoUnitPrice;

    public static class ProduceTypeConverter implements PropertyConverter<EnumPotatoProduceType, String> {

        @Override
        public EnumPotatoProduceType convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return EnumPotatoProduceType.TUBERS;
            }
            for (EnumPotatoProduceType produceType : EnumPotatoProduceType.values()) {
                if (produceType.produce().equalsIgnoreCase(databaseValue)) {
                    return produceType;
                }
            }
            return EnumPotatoProduceType.TUBERS;
        }

        @Override
        public String convertToDatabaseValue(EnumPotatoProduceType enumPotatoProduceType) {
            return enumPotatoProduceType == null ? null : enumPotatoProduceType.produce();
        }
    }

    public static class UnitPriceConverter implements PropertyConverter<EnumPotatoUnitPrice, Double> {

        @Override
        public EnumPotatoUnitPrice convertToEntityProperty(Double databaseValue) {
            if (databaseValue == null) {
                return EnumPotatoUnitPrice.UNKNOWN;
            }
            for (EnumPotatoUnitPrice enumUnitPrice : EnumPotatoUnitPrice.values()) {
                if (enumUnitPrice.convertToLocalCurrency(EnumCountries.OTHERS.currency(),null) == databaseValue) {
                    return enumUnitPrice;
                }
            }
            return EnumPotatoUnitPrice.UNKNOWN;
        }

        @Override
        public Double convertToDatabaseValue(EnumPotatoUnitPrice enumUnitPrice) {
            return enumUnitPrice == null ? null : enumUnitPrice.convertToLocalCurrency(EnumCountries.OTHERS.currency(),null);
        }
    }

    public static class UnitOfSaleConverter implements PropertyConverter<EnumUnitOfSale, Integer> {

        @Override
        public EnumUnitOfSale convertToEntityProperty(Integer databaseValue) {
            if (databaseValue == null) {
                return EnumUnitOfSale.UNIT_FIFTY_KG;
            }
            for (EnumUnitOfSale unitOfSale : EnumUnitOfSale.values()) {
                if (unitOfSale.unitWeight() == databaseValue) {
                    return unitOfSale;
                }
            }
            return EnumUnitOfSale.UNIT_FIFTY_KG;
        }

        @Override
        public Integer convertToDatabaseValue(EnumUnitOfSale enumUnitOfSale) {
            return enumUnitOfSale == null ? null : enumUnitOfSale.unitWeight();
        }
    }
}
