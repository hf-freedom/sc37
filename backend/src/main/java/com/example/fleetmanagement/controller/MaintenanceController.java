package com.example.fleetmanagement.controller;

import com.example.fleetmanagement.model.MaintenanceOrder;
import com.example.fleetmanagement.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin(origins = "*")
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;

    @GetMapping
    public List<MaintenanceOrder> getAllMaintenanceOrders() {
        return maintenanceService.getAllMaintenanceOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceOrder> getMaintenanceOrderById(@PathVariable Long id) {
        Optional<MaintenanceOrder> order = maintenanceService.getMaintenanceOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public MaintenanceOrder createMaintenanceOrder(@RequestBody MaintenanceOrder order) {
        return maintenanceService.createMaintenanceOrder(order);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<MaintenanceOrder> startMaintenance(@PathVariable Long id) {
        try {
            MaintenanceOrder updated = maintenanceService.startMaintenance(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<MaintenanceOrder> completeMaintenance(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            Double cost = request.get("cost") != null ? 
                    Double.valueOf(request.get("cost").toString()) : 0.0;
            String notes = (String) request.getOrDefault("notes", "");
            
            MaintenanceOrder updated = maintenanceService.completeMaintenance(id, cost, notes);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<MaintenanceOrder> cancelMaintenance(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            String reason = request != null ? (String) request.getOrDefault("reason", "") : "";
            MaintenanceOrder updated = maintenanceService.cancelMaintenance(id, reason);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
