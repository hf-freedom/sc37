package com.example.fleetmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceOrder {
    private Long id;
    private Long vehicleId;
    private String vehiclePlateNumber;
    private MaintenanceType type;
    private MaintenanceStatus status;
    private String description;
    private Double cost;
    private LocalDateTime scheduledDate;
    private LocalDateTime startDate;
    private LocalDateTime completionDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
