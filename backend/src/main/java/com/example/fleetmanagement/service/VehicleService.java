package com.example.fleetmanagement.service;

import com.example.fleetmanagement.model.Vehicle;
import com.example.fleetmanagement.model.VehicleStatus;
import com.example.fleetmanagement.model.VehicleType;
import com.example.fleetmanagement.repository.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    @Autowired
    private InMemoryDataStore dataStore;

    public List<Vehicle> getAllVehicles() {
        return dataStore.findAllVehicles();
    }

    public Optional<Vehicle> getVehicleById(Long id) {
        return dataStore.findVehicleById(id);
    }

    public Vehicle createVehicle(Vehicle vehicle) {
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.setCreatedAt(LocalDateTime.now());
        vehicle.setUpdatedAt(LocalDateTime.now());
        return dataStore.saveVehicle(vehicle);
    }

    public Vehicle updateVehicle(Long id, Vehicle updatedVehicle) {
        return dataStore.findVehicleById(id)
                .map(vehicle -> {
                    vehicle.setPlateNumber(updatedVehicle.getPlateNumber());
                    vehicle.setType(updatedVehicle.getType());
                    vehicle.setSeats(updatedVehicle.getSeats());
                    vehicle.setCurrentMileage(updatedVehicle.getCurrentMileage());
                    vehicle.setMaintenanceCycle(updatedVehicle.getMaintenanceCycle());
                    vehicle.setLastMaintenanceMileage(updatedVehicle.getLastMaintenanceMileage());
                    vehicle.setNextMaintenanceDate(updatedVehicle.getNextMaintenanceDate());
                    vehicle.setUpdatedAt(LocalDateTime.now());
                    return dataStore.saveVehicle(vehicle);
                })
                .orElseThrow(() -> new RuntimeException("车辆不存在"));
    }

    public void deleteVehicle(Long id) {
        dataStore.deleteVehicle(id);
    }

    public List<Vehicle> getAvailableVehicles() {
        return dataStore.findAllVehicles().stream()
                .filter(v -> v.getStatus() == VehicleStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    public List<Vehicle> recommendVehicles(int passengerCount, VehicleType preferredType) {
        List<Vehicle> availableVehicles = getAvailableVehicles();
        
        return availableVehicles.stream()
                .filter(v -> v.getSeats() >= passengerCount)
                .sorted((v1, v2) -> {
                    if (preferredType != null) {
                        boolean v1Match = v1.getType() == preferredType;
                        boolean v2Match = v2.getType() == preferredType;
                        if (v1Match != v2Match) {
                            return v1Match ? -1 : 1;
                        }
                    }
                    return Integer.compare(v1.getSeats() - passengerCount, v2.getSeats() - passengerCount);
                })
                .collect(Collectors.toList());
    }

    public boolean checkMaintenanceNeeded(Vehicle vehicle) {
        double mileageSinceMaintenance = vehicle.getCurrentMileage() - vehicle.getLastMaintenanceMileage();
        return mileageSinceMaintenance > vehicle.getMaintenanceCycle();
    }

    public Vehicle updateVehicleStatus(Long id, VehicleStatus status) {
        return dataStore.findVehicleById(id)
                .map(vehicle -> {
                    vehicle.setStatus(status);
                    vehicle.setUpdatedAt(LocalDateTime.now());
                    return dataStore.saveVehicle(vehicle);
                })
                .orElseThrow(() -> new RuntimeException("车辆不存在"));
    }
}
