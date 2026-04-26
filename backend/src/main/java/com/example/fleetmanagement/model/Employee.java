package com.example.fleetmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    private Long id;
    private String name;
    private String department;
    private String phone;
    private String position;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
