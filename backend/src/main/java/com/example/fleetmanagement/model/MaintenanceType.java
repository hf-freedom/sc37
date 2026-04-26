package com.example.fleetmanagement.model;

public enum MaintenanceType {
    REGULAR_MAINTENANCE("常规保养"),
    REPAIR("维修"),
    INSPECTION("年检"),
    OTHER("其他");

    private final String description;

    MaintenanceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
