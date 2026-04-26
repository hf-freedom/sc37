package com.example.fleetmanagement.model;

public enum DriverStatus {
    AVAILABLE("可用"),
    ON_DUTY("出车中"),
    ON_LEAVE("请假"),
    UNAVAILABLE("不可用");

    private final String description;

    DriverStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
