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
        @Override
        public String currencyName() {
            return "Naira";
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
        @Override
        public String currencyName() {
            return "Tanzania shillings";
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
        @Override
        public String currencyName() {
            return "Kenya shillings";
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

        @Override
        public String currencyName() {
            return "US Dollars";
        }
    };


    public abstract String countryCode();

    public abstract String countryName();

    public abstract String currency();

    public abstract String currencyName();
}
