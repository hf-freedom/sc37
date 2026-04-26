package com.example.fleetmanagement.service;

import com.example.fleetmanagement.model.*;
import com.example.fleetmanagement.repository.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MaintenanceService {

    @Autowired
    private InMemoryDataStore dataStore;

    @Autowired
    private VehicleService vehicleService;

    public List<MaintenanceOrder> getAllMaintenanceOrders() {
        return dataStore.findAllMaintenanceOrders();
    }

    public Optional<MaintenanceOrder> getMaintenanceOrderById(Long id) {
        return dataStore.findMaintenanceOrderById(id);
    }

    public MaintenanceOrder createMaintenanceOrder(MaintenanceOrder order) {
        order.setStatus(MaintenanceStatus.SCHEDULED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        if (order.getVehicleId() != null) {
            Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(order.getVehicleId());
            if (vehicleOpt.isPresent()) {
                Vehicle vehicle = vehicleOpt.get();
                order.setVehiclePlateNumber(vehicle.getPlateNumber());
                vehicleService.updateVehicleStatus(vehicle.getId(), VehicleStatus.MAINTENANCE);
            }
        }

        return dataStore.saveMaintenanceOrder(order);
    }

    public MaintenanceOrder createMaintenanceOrderFromVehicle(Vehicle vehicle) {
        MaintenanceOrder order = new MaintenanceOrder();
        order.setVehicleId(vehicle.getId());
        order.setVehiclePlateNumber(vehicle.getPlateNumber());
        order.setType(MaintenanceType.REGULAR_MAINTENANCE);
        order.setStatus(MaintenanceStatus.SCHEDULED);
        order.setDescription(String.format("自动生成保养工单 - 当前里程: %.1f公里, 保养周期: %.1f公里",
                vehicle.getCurrentMileage(), vehicle.getMaintenanceCycle()));
        order.setScheduledDate(LocalDateTime.now().plusDays(1));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        vehicleService.updateVehicleStatus(vehicle.getId(), VehicleStatus.MAINTENANCE);

        return dataStore.saveMaintenanceOrder(order);
    }

    public MaintenanceOrder startMaintenance(Long orderId) {
        return dataStore.findMaintenanceOrderById(orderId)
                .map(order -> {
                    if (order.getStatus() != MaintenanceStatus.SCHEDULED) {
                        throw new RuntimeException("只有已安排状态的工单才能开始");
                    }
                    order.setStatus(MaintenanceStatus.IN_PROGRESS);
                    order.setStartDate(LocalDateTime.now());
                    order.setUpdatedAt(LocalDateTime.now());
                    return dataStore.saveMaintenanceOrder(order);
                })
                .orElseThrow(() -> new RuntimeException("工单不存在"));
    }

    public MaintenanceOrder completeMaintenance(Long orderId, Double cost, String notes) {
        return dataStore.findMaintenanceOrderById(orderId)
                .map(order -> {
                    if (order.getStatus() != MaintenanceStatus.IN_PROGRESS) {
                        throw new RuntimeException("只有进行中的工单才能完成");
                    }
                    order.setStatus(MaintenanceStatus.COMPLETED);
                    order.setCompletionDate(LocalDateTime.now());
                    order.setCost(cost);
                    order.setNotes(notes);
                    order.setUpdatedAt(LocalDateTime.now());

                    if (order.getVehicleId() != null) {
                        Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(order.getVehicleId());
                        if (vehicleOpt.isPresent()) {
                            Vehicle vehicle = vehicleOpt.get();
                            vehicle.setLastMaintenanceMileage(vehicle.getCurrentMileage());
                            vehicleService.updateVehicleStatus(vehicle.getId(), VehicleStatus.AVAILABLE);
                            dataStore.saveVehicle(vehicle);
                        }
                    }

                    return dataStore.saveMaintenanceOrder(order);
                })
                .orElseThrow(() -> new RuntimeException("工单不存在"));
    }

    public MaintenanceOrder cancelMaintenance(Long orderId, String reason) {
        return dataStore.findMaintenanceOrderById(orderId)
                .map(order -> {
                    if (order.getStatus() == MaintenanceStatus.COMPLETED) {
                        throw new RuntimeException("已完成的工单无法取消");
                    }
                    order.setStatus(MaintenanceStatus.CANCELLED);
                    order.setNotes(reason);
                    order.setUpdatedAt(LocalDateTime.now());

                    if (order.getVehicleId() != null) {
                        Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(order.getVehicleId());
                        if (vehicleOpt.isPresent()) {
                            Vehicle vehicle = vehicleOpt.get();
                            Optional<MaintenanceOrder> activeOrderOpt = dataStore.findAllMaintenanceOrders().stream()
                                    .filter(o -> !o.getId().equals(orderId))
                                    .filter(o -> o.getVehicleId() != null && o.getVehicleId().equals(vehicle.getId()))
                                    .filter(o -> o.getStatus() == MaintenanceStatus.SCHEDULED || o.getStatus() == MaintenanceStatus.IN_PROGRESS)
                                    .findFirst();
                            
                            if (!activeOrderOpt.isPresent()) {
                                vehicleService.updateVehicleStatus(vehicle.getId(), VehicleStatus.AVAILABLE);
                            }
                        }
                    }

                    return dataStore.saveMaintenanceOrder(order);
                })
                .orElseThrow(() -> new RuntimeException("工单不存在"));
    }
}
