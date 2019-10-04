package com.iita.akilimo.utils.enums;

public enum EnumFragmentTitle {
    title_location {
        @Override
        public String title() {
            return "MyLocation";
        }
    },
    title_area_units {
        @Override
        public String title() {
            return "Area Units";
        }
    },
    title_cropping_system {
        @Override
        public String title() {
            return "Cropping System";
        }
    },
    title_advice {
        @Override
        public String title() {
            return "Advice";
        }
    },
    title_field_area {
        @Override
        public String title() {
            return "Field area";
        }
    },
    title_planting_date {
        @Override
        public String title() {
            return "Planting title";
        }
    },
    title_harvest_date {
        @Override
        public String title() {
            return "Harvest title";
        }
    },
    title_tractor {
        @Override
        public String title() {
            return "Tractor";
        }
    },
    title_herbicides {
        @Override
        public String title() {
            return "Herbicides";
        }
    },
    title_current_practice {
        @Override
        public String title() {
            return "Current practice";
        }
    },
    title_cost_of_operations {
        @Override
        public String title() {
            return "Advice";
        }
    },
    title_yield_type {
        @Override
        public String title() {
            return "Yield name";
        }
    },
    title_curent_field_yield {
        @Override
        public String title() {
            return "Field yield";
        }
    },
    title_maize_performance {
        @Override
        public String title() {
            return "Maize performance";
        }
    },
    title_fertilizer_types {
        @Override
        public String title() {
            return "Fertilizers";
        }
    },
    title_fertilizer_prices {
        @Override
        public String title() {
            return "Fertilizer prices";
        }
    },
    title_starch_factory_sale {
        @Override
        public String title() {
            return "Starch factory sale";
        }
    },
    title_unit_of_sale {
        @Override
        public String title() {
            return "Unit of sale";
        }
    },
    title_unit_price {
        @Override
        public String title() {
            return "Unit price";
        }
    },
    title_unit_of_sale_maize {
        @Override
        public String title() {
            return "Unit of sale (maize)";
        }
    },
    title_investment {
        @Override
        public String title() {
            return "Investment";
        }
    },
    title_recommendations {
        @Override
        public String title() {
            return "Recommendations";
        }
    };

    public abstract String title();
}
