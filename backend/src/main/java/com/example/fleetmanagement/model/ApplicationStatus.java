package com.example.fleetmanagement.model;

public enum ApplicationStatus {
    DRAFT("草稿"),
    SUBMITTED("已提交待审批"),
    APPROVED("审批通过"),
    REJECTED("审批驳回"),
    IN_PROGRESS("出车中"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    NEEDS_REVIEW("费用待复核");

    private final String description;

    ApplicationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
