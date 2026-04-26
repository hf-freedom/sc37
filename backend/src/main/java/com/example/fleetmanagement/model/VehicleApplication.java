package com.example.fleetmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleApplication {
    private Long id;
    private Long applicantId;
    private String applicantName;
    private String applicantDepartment;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String destination;
    private Integer passengerCount;
    private String purpose;
    private Double estimatedMileage;
    private ApplicationStatus status;
    private Long assignedVehicleId;
    private Long assignedDriverId;
    private Double actualMileage;
    private Double fuelCost;
    private Double tollCost;
    private Double otherCost;
    private Boolean needsReview;
    private String reviewReason;
    private List<ApprovalRecord> approvalRecords;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
