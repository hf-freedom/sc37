package com.example.fleetmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReport {
    private Long id;
    private String month;
    private Long totalTrips;
    private Double totalMileage;
    private Double totalFuelCost;
    private Double totalTollCost;
    private Double totalMaintenanceCost;
    private Double totalCost;
    private Double vehicleUtilizationRate;
    private Integer monthDays;

    public void setMonthFromYearMonth(YearMonth yearMonth) {
        this.month = yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}
