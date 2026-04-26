package com.example.fleetmanagement.controller;

import com.example.fleetmanagement.model.VehicleApplication;
import com.example.fleetmanagement.service.VehicleApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class VehicleApplicationController {

    @Autowired
    private VehicleApplicationService applicationService;

    @GetMapping
    public List<VehicleApplication> getAllApplications() {
        return applicationService.getAllApplications();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleApplication> getApplicationById(@PathVariable Long id) {
        Optional<VehicleApplication> application = applicationService.getApplicationById(id);
        return application.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public VehicleApplication createApplication(@RequestBody VehicleApplication application) {
        return applicationService.createApplication(application);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<VehicleApplication> submitApplication(@PathVariable Long id) {
        try {
            VehicleApplication updated = applicationService.submitApplication(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<VehicleApplication> approveApplication(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            Long approverId = request.get("approverId") != null ? 
                    Long.valueOf(request.get("approverId").toString()) : 1L;
            String approverName = (String) request.getOrDefault("approverName", "系统管理员");
            String comment = (String) request.getOrDefault("comment", "");
            
            VehicleApplication updated = applicationService.approveApplication(id, approverId, approverName, comment);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<VehicleApplication> rejectApplication(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            Long approverId = request.get("approverId") != null ? 
                    Long.valueOf(request.get("approverId").toString()) : 1L;
            String approverName = (String) request.getOrDefault("approverName", "系统管理员");
            String comment = (String) request.getOrDefault("comment", "");
            
            VehicleApplication updated = applicationService.rejectApplication(id, approverId, approverName, comment);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<VehicleApplication> assignVehicleAndDriver(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            Long vehicleId = Long.valueOf(request.get("vehicleId").toString());
            Long driverId = Long.valueOf(request.get("driverId").toString());
            
            VehicleApplication updated = applicationService.assignVehicleAndDriver(id, vehicleId, driverId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<VehicleApplication> startTrip(@PathVariable Long id) {
        try {
            VehicleApplication updated = applicationService.startTrip(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<VehicleApplication> completeTrip(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            Double actualMileage = request.get("actualMileage") != null ? 
                    Double.valueOf(request.get("actualMileage").toString()) : 0.0;
            Double fuelCost = request.get("fuelCost") != null ? 
                    Double.valueOf(request.get("fuelCost").toString()) : 0.0;
            Double tollCost = request.get("tollCost") != null ? 
                    Double.valueOf(request.get("tollCost").toString()) : 0.0;
            Double otherCost = request.get("otherCost") != null ? 
                    Double.valueOf(request.get("otherCost").toString()) : 0.0;
            
            VehicleApplication updated = applicationService.completeTrip(id, actualMileage, fuelCost, tollCost, otherCost);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<VehicleApplication> reviewExpense(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            boolean approved = (Boolean) request.getOrDefault("approved", true);
            String comment = (String) request.getOrDefault("comment", "");
            
            VehicleApplication updated = applicationService.reviewExpense(id, approved, comment);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<VehicleApplication> cancelApplication(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            String reason = request != null ? (String) request.getOrDefault("reason", "") : "";
            VehicleApplication updated = applicationService.cancelApplication(id, reason);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
