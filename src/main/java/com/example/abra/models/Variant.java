package com.example.abra.models;

import java.util.List;

public class Variant {

    private class Weight {

        private int value;

        public Weight(int value) {
            if (value < 0) {
                throw new IllegalArgumentException("Weight cannot be negative");
            }
            if (value > 100) {
                throw new IllegalArgumentException("Weight cannot exceed 100");
            }
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public String name;
    public Weight weight;
    public List<Endpoint> endpoints;
}
