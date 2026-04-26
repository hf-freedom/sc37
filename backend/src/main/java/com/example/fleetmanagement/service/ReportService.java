package com.example.fleetmanagement.service;

import com.example.fleetmanagement.model.*;
import com.example.fleetmanagement.repository.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private InMemoryDataStore dataStore;

    public List<MonthlyReport> getAllMonthlyReports() {
        return dataStore.findAllMonthlyReports();
    }

    public MonthlyReport generateMonthlyReport(YearMonth month) {
        List<VehicleApplication> completedApplications = dataStore.findAllApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.COMPLETED ||
                        app.getStatus() == ApplicationStatus.NEEDS_REVIEW)
                .filter(app -> {
                    if (app.getUpdatedAt() == null) return false;
                    YearMonth appMonth = YearMonth.from(app.getUpdatedAt());
                    return appMonth.equals(month);
                })
                .collect(Collectors.toList());

        long totalTrips = completedApplications.size();
        double totalMileage = completedApplications.stream()
                .filter(app -> app.getActualMileage() != null)
                .mapToDouble(VehicleApplication::getActualMileage)
                .sum();
        double totalFuelCost = completedApplications.stream()
                .filter(app -> app.getFuelCost() != null)
                .mapToDouble(VehicleApplication::getFuelCost)
                .sum();
        double totalTollCost = completedApplications.stream()
                .filter(app -> app.getTollCost() != null)
                .mapToDouble(VehicleApplication::getTollCost)
                .sum();
        double totalOtherCost = completedApplications.stream()
                .filter(app -> app.getOtherCost() != null)
                .mapToDouble(VehicleApplication::getOtherCost)
                .sum();

        List<MaintenanceOrder> completedMaintenance = dataStore.findAllMaintenanceOrders().stream()
                .filter(order -> order.getStatus() == MaintenanceStatus.COMPLETED)
                .filter(order -> {
                    if (order.getCompletionDate() == null) return false;
                    YearMonth orderMonth = YearMonth.from(order.getCompletionDate());
                    return orderMonth.equals(month);
                })
                .collect(Collectors.toList());

        double totalMaintenanceCost = completedMaintenance.stream()
                .filter(order -> order.getCost() != null)
                .mapToDouble(MaintenanceOrder::getCost)
                .sum();

        double totalCost = totalFuelCost + totalTollCost + totalOtherCost + totalMaintenanceCost;

        List<Vehicle> allVehicles = dataStore.findAllVehicles();
        int totalVehicles = allVehicles.size();
        int monthDays = month.lengthOfMonth();
        
        double vehicleUtilizationRate = 0.0;
        if (totalVehicles > 0 && monthDays > 0) {
            double totalPossibleDays = totalVehicles * monthDays;
            if (totalPossibleDays > 0) {
                vehicleUtilizationRate = Math.min(100.0, (totalTrips * 1.0 / totalPossibleDays) * 100);
            }
        }

        MonthlyReport report = new MonthlyReport();
        report.setMonthFromYearMonth(month);
        report.setTotalTrips(totalTrips);
        report.setTotalMileage(totalMileage);
        report.setTotalFuelCost(totalFuelCost);
        report.setTotalTollCost(totalTollCost);
        report.setTotalMaintenanceCost(totalMaintenanceCost);
        report.setTotalCost(totalCost);
        report.setVehicleUtilizationRate(vehicleUtilizationRate);
        report.setMonthDays(monthDays);

        return dataStore.saveMonthlyReport(report);
    }
}
