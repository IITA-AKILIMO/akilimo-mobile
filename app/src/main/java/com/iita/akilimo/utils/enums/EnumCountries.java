package com.iita.akilimo.utils.enums;

public enum EnumCountries {
    NIGERIA {
        @Override
        public String countryName() {
            return "Nigeria";
        }

        @Override
        public String countryCode() {
            return "NG";
        }

        @Override
        public String currency() {
            return "NGN";
        }
    },
    TANZANIA {
        @Override
        public String countryName() {
            return "Tanzania";
        }

        @Override
        public String countryCode() {
            return "TZ";
        }

        @Override
        public String currency() {
            return "TZS";
        }
    },
    KENYA {
        @Override
        public String countryName() {
            return "Kenya";
        }

        @Override
        public String countryCode() {
            return "KE";
        }

        @Override
        public String currency() {
            return "KES";
        }
    },
    OTHERS {
        @Override
        public String countryName() {
            return "U.S.A";
        }

        @Override
        public String countryCode() {
            return "US";
        }

        @Override
        public String currency() {
            return "USD";
        }
    };


    public abstract String countryCode();

    public abstract String countryName();

    public abstract String currency();
}
