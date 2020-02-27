package com.iita.akilimo.entities;

import com.iita.akilimo.utils.enums.EnumGender;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.converter.PropertyConverter;
import lombok.Data;

@Data
@Entity
public class ProfileInfo {
    @Id
    long id;

    public String firstName;
    public String lastName;
    public String email;
    public String mobile;
    public String farmName;

    @Convert(converter = GenderConverter.class, dbType = String.class)
    public EnumGender genderEnum;

    public String getNames() {
        return String.format("%s %s", this.firstName, this.lastName);
    }

    /* converter for custom data type*/
    public static class GenderConverter implements PropertyConverter<EnumGender, String> {

        @Override
        public EnumGender convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return EnumGender.OTHER;
            }
            for (EnumGender role : EnumGender.values()) {
                if (role.name().equalsIgnoreCase(databaseValue)) {
                    return role;
                }
            }
            return EnumGender.OTHER;
        }

        @Override
        public String convertToDatabaseValue(EnumGender entityProperty) {
            return entityProperty == null ? null : entityProperty.name();
        }
    }
}
