package com.example.fleetmanagement.model;

public enum ApprovalStatus {
    PENDING("待审批"),
    APPROVED("已通过"),
    REJECTED("已驳回");

    private final String description;

    ApprovalStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
