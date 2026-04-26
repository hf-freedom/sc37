package com.example.fleetmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {
    private Long id;
    private String plateNumber;
    private VehicleType type;
    private Integer seats;
    private VehicleStatus status;
    private Double currentMileage;
    private Double maintenanceCycle;
    private Double lastMaintenanceMileage;
    private LocalDateTime nextMaintenanceDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
