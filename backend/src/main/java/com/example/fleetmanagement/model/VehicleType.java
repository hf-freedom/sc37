package com.example.fleetmanagement.model;

public enum VehicleType {
    SEDAN("轿车", 5),
    SUV("SUV", 7),
    VAN("面包车", 9),
    BUS("大巴", 45);

    private final String description;
    private final int defaultSeats;

    VehicleType(String description, int defaultSeats) {
        this.description = description;
        this.defaultSeats = defaultSeats;
    }

    public String getDescription() {
        return description;
    }

    public int getDefaultSeats() {
        return defaultSeats;
    }
}
