package com.example.fleetmanagement.model;

public enum VehicleStatus {
    AVAILABLE("可用"),
    IN_USE("使用中"),
    MAINTENANCE("维修保养中"),
    UNAVAILABLE("不可用");

    private final String description;

    VehicleStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
