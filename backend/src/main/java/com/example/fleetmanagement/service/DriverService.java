package com.example.fleetmanagement.service;

import com.example.fleetmanagement.model.Driver;
import com.example.fleetmanagement.model.DriverStatus;
import com.example.fleetmanagement.model.VehicleType;
import com.example.fleetmanagement.repository.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DriverService {

    @Autowired
    private InMemoryDataStore dataStore;

    public List<Driver> getAllDrivers() {
        return dataStore.findAllDrivers();
    }

    public Optional<Driver> getDriverById(Long id) {
        return dataStore.findDriverById(id);
    }

    public Driver createDriver(Driver driver) {
        driver.setStatus(DriverStatus.AVAILABLE);
        driver.setCurrentTaskCount(0);
        driver.setCreatedAt(LocalDateTime.now());
        driver.setUpdatedAt(LocalDateTime.now());
        return dataStore.saveDriver(driver);
    }

    public Driver updateDriver(Long id, Driver updatedDriver) {
        return dataStore.findDriverById(id)
                .map(driver -> {
                    driver.setName(updatedDriver.getName());
                    driver.setPhone(updatedDriver.getPhone());
                    driver.setLicenseNumber(updatedDriver.getLicenseNumber());
                    driver.setAllowedVehicleTypes(updatedDriver.getAllowedVehicleTypes());
                    driver.setAvailableFrom(updatedDriver.getAvailableFrom());
                    driver.setAvailableUntil(updatedDriver.getAvailableUntil());
                    driver.setUpdatedAt(LocalDateTime.now());
                    return dataStore.saveDriver(driver);
                })
                .orElseThrow(() -> new RuntimeException("司机不存在"));
    }

    public void deleteDriver(Long id) {
        dataStore.deleteDriver(id);
    }

    public List<Driver> getAvailableDrivers() {
        return dataStore.findAllDrivers().stream()
                .filter(d -> d.getStatus() == DriverStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    public List<Driver> getAvailableDriversForVehicleType(VehicleType vehicleType) {
        return getAvailableDrivers().stream()
                .filter(d -> d.getAllowedVehicleTypes().contains(vehicleType))
                .collect(Collectors.toList());
    }

    public Driver assignTask(Long driverId) {
        return dataStore.findDriverById(driverId)
                .map(driver -> {
                    driver.setCurrentTaskCount(driver.getCurrentTaskCount() + 1);
                    driver.setStatus(DriverStatus.ON_DUTY);
                    driver.setUpdatedAt(LocalDateTime.now());
                    return dataStore.saveDriver(driver);
                })
                .orElseThrow(() -> new RuntimeException("司机不存在"));
    }

    public Driver releaseTask(Long driverId) {
        return dataStore.findDriverById(driverId)
                .map(driver -> {
                    int newCount = Math.max(0, driver.getCurrentTaskCount() - 1);
                    driver.setCurrentTaskCount(newCount);
                    if (newCount == 0) {
                        driver.setStatus(DriverStatus.AVAILABLE);
                    }
                    driver.setUpdatedAt(LocalDateTime.now());
                    return dataStore.saveDriver(driver);
                })
                .orElseThrow(() -> new RuntimeException("司机不存在"));
    }

    public Driver updateDriverStatus(Long id, DriverStatus status) {
        return dataStore.findDriverById(id)
                .map(driver -> {
                    driver.setStatus(status);
                    driver.setUpdatedAt(LocalDateTime.now());
                    return dataStore.saveDriver(driver);
                })
                .orElseThrow(() -> new RuntimeException("司机不存在"));
    }
}
