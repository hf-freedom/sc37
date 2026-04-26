package com.example.fleetmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRecord {
    private Long id;
    private Long applicationId;
    private Long approverId;
    private String approverName;
    private ApprovalStatus status;
    private String comment;
    private LocalDateTime approvedAt;
}
