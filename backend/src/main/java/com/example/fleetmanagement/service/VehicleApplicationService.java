package com.example.fleetmanagement.service;

import com.example.fleetmanagement.model.*;
import com.example.fleetmanagement.repository.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleApplicationService {

    private static final double MILEAGE_OVERAGE_THRESHOLD = 0.2;

    @Autowired
    private InMemoryDataStore dataStore;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private DriverService driverService;

    @Autowired
    private MaintenanceService maintenanceService;

    public List<VehicleApplication> getAllApplications() {
        return dataStore.findAllApplications();
    }

    public Optional<VehicleApplication> getApplicationById(Long id) {
        return dataStore.findApplicationById(id);
    }

    public VehicleApplication createApplication(VehicleApplication application) {
        application.setStatus(ApplicationStatus.DRAFT);
        application.setNeedsReview(false);
        application.setApprovalRecords(new ArrayList<>());
        application.setCreatedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        return dataStore.saveApplication(application);
    }

    public VehicleApplication submitApplication(Long applicationId) {
        return dataStore.findApplicationById(applicationId)
                .map(application -> {
                    if (application.getStatus() != ApplicationStatus.DRAFT) {
                        throw new RuntimeException("只有草稿状态的申请才能提交");
                    }
                    application.setStatus(ApplicationStatus.SUBMITTED);
                    application.setUpdatedAt(LocalDateTime.now());
                    return dataStore.saveApplication(application);
                })
                .orElseThrow(() -> new RuntimeException("申请不存在"));
    }

    public VehicleApplication approveApplication(Long applicationId, Long approverId, String approverName, String comment) {
        return dataStore.findApplicationById(applicationId)
                .map(application -> {
                    if (application.getStatus() != ApplicationStatus.SUBMITTED) {
                        throw new RuntimeException("只有已提交待审批状态的申请才能审批");
                    }

                    ApprovalRecord approvalRecord = new ApprovalRecord();
                    approvalRecord.setApplicationId(applicationId);
                    approvalRecord.setApproverId(approverId);
                    approvalRecord.setApproverName(approverName);
                    approvalRecord.setStatus(ApprovalStatus.APPROVED);
                    approvalRecord.setComment(comment);
                    approvalRecord.setApprovedAt(LocalDateTime.now());

                    if (application.getApprovalRecords() == null) {
                        application.setApprovalRecords(new ArrayList<>());
                    }
                    application.getApprovalRecords().add(approvalRecord);
                    application.setStatus(ApplicationStatus.APPROVED);
                    application.setUpdatedAt(LocalDateTime.now());

                    return dataStore.saveApplication(application);
                })
                .orElseThrow(() -> new RuntimeException("申请不存在"));
    }

    public VehicleApplication rejectApplication(Long applicationId, Long approverId, String approverName, String comment) {
        return dataStore.findApplicationById(applicationId)
                .map(application -> {
                    if (application.getStatus() != ApplicationStatus.SUBMITTED) {
                        throw new RuntimeException("只有已提交待审批状态的申请才能审批");
                    }

                    ApprovalRecord approvalRecord = new ApprovalRecord();
                    approvalRecord.setApplicationId(applicationId);
                    approvalRecord.setApproverId(approverId);
                    approvalRecord.setApproverName(approverName);
                    approvalRecord.setStatus(ApprovalStatus.REJECTED);
                    approvalRecord.setComment(comment);
                    approvalRecord.setApprovedAt(LocalDateTime.now());

                    if (application.getApprovalRecords() == null) {
                        application.setApprovalRecords(new ArrayList<>());
                    }
                    application.getApprovalRecords().add(approvalRecord);
                    application.setStatus(ApplicationStatus.REJECTED);
                    application.setUpdatedAt(LocalDateTime.now());

                    return dataStore.saveApplication(application);
                })
                .orElseThrow(() -> new RuntimeException("申请不存在"));
    }

    public VehicleApplication assignVehicleAndDriver(Long applicationId, Long vehicleId, Long driverId) {
        return dataStore.findApplicationById(applicationId)
                .map(application -> {
                    if (application.getStatus() != ApplicationStatus.APPROVED) {
                        throw new RuntimeException("只有审批通过的申请才能派车");
                    }

                    Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(vehicleId);
                    if (!vehicleOpt.isPresent() || vehicleOpt.get().getStatus() != VehicleStatus.AVAILABLE) {
                        throw new RuntimeException("车辆不可用");
                    }
                    Vehicle vehicle = vehicleOpt.get();

                    Optional<Driver> driverOpt = driverService.getDriverById(driverId);
                    if (!driverOpt.isPresent() || driverOpt.get().getStatus() != DriverStatus.AVAILABLE) {
                        throw new RuntimeException("司机不可用");
                    }
                    Driver driver = driverOpt.get();

                    if (vehicle.getSeats() < application.getPassengerCount()) {
                        throw new RuntimeException(String.format("车辆座位数(%d座)不足以容纳申请人数(%d人)",
                                vehicle.getSeats(), application.getPassengerCount()));
                    }

                    if (driver.getAllowedVehicleTypes() == null ||
                            !driver.getAllowedVehicleTypes().contains(vehicle.getType())) {
                        throw new RuntimeException(String.format("司机准驾车型不匹配：司机准驾车型不包含%s",
                                vehicle.getType()));
                    }

                    application.setAssignedVehicleId(vehicleId);
                    application.setAssignedDriverId(driverId);
                    application.setUpdatedAt(LocalDateTime.now());

                    vehicleService.updateVehicleStatus(vehicleId, VehicleStatus.IN_USE);
                    driverService.assignTask(driverId);

                    return dataStore.saveApplication(application);
                })
                .orElseThrow(() -> new RuntimeException("申请不存在"));
    }

    public VehicleApplication startTrip(Long applicationId) {
        return dataStore.findApplicationById(applicationId)
                .map(application -> {
                    if (application.getStatus() != ApplicationStatus.APPROVED || application.getAssignedVehicleId() == null) {
                        throw new RuntimeException("请先派车后再开始行程");
                    }

                    Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(application.getAssignedVehicleId());
                    if (vehicleOpt.isPresent() && vehicleService.checkMaintenanceNeeded(vehicleOpt.get())) {
                        throw new RuntimeException("车辆需要保养，请先安排保养");
                    }

                    application.setStatus(ApplicationStatus.IN_PROGRESS);
                    application.setUpdatedAt(LocalDateTime.now());

                    return dataStore.saveApplication(application);
                })
                .orElseThrow(() -> new RuntimeException("申请不存在"));
    }

    public VehicleApplication completeTrip(Long applicationId, Double actualMileage, Double fuelCost, Double tollCost, Double otherCost) {
        return dataStore.findApplicationById(applicationId)
                .map(application -> {
                    if (application.getStatus() != ApplicationStatus.IN_PROGRESS) {
                        throw new RuntimeException("只有进行中的行程才能完成");
                    }

                    application.setActualMileage(actualMileage);
                    application.setFuelCost(fuelCost);
                    application.setTollCost(tollCost);
                    application.setOtherCost(otherCost);

                    if (application.getEstimatedMileage() != null && application.getEstimatedMileage() > 0) {
                        double overageRatio = (actualMileage - application.getEstimatedMileage()) / application.getEstimatedMileage();
                        if (overageRatio > MILEAGE_OVERAGE_THRESHOLD) {
                            application.setStatus(ApplicationStatus.NEEDS_REVIEW);
                            application.setNeedsReview(true);
                            application.setReviewReason(String.format("实际里程(%.1f公里)超过预估里程(%.1f公里)的%.0f%%",
                                    actualMileage, application.getEstimatedMileage(), MILEAGE_OVERAGE_THRESHOLD * 100));
                        } else {
                            application.setStatus(ApplicationStatus.COMPLETED);
                        }
                    } else {
                        application.setStatus(ApplicationStatus.COMPLETED);
                    }

                    if (application.getAssignedVehicleId() != null) {
                        Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(application.getAssignedVehicleId());
                        if (vehicleOpt.isPresent()) {
                            Vehicle vehicle = vehicleOpt.get();
                            vehicle.setCurrentMileage(vehicle.getCurrentMileage() + actualMileage);
                            vehicleService.updateVehicleStatus(vehicle.getId(), VehicleStatus.AVAILABLE);
                            dataStore.saveVehicle(vehicle);

                            if (vehicleService.checkMaintenanceNeeded(vehicle)) {
                                maintenanceService.createMaintenanceOrderFromVehicle(vehicle);
                            }
                        }
                    }

                    if (application.getAssignedDriverId() != null) {
                        driverService.releaseTask(application.getAssignedDriverId());
                    }

                    application.setUpdatedAt(LocalDateTime.now());
                    return dataStore.saveApplication(application);
                })
                .orElseThrow(() -> new RuntimeException("申请不存在"));
    }

    public VehicleApplication reviewExpense(Long applicationId, boolean approved, String comment) {
        return dataStore.findApplicationById(applicationId)
                .map(application -> {
                    if (application.getStatus() != ApplicationStatus.NEEDS_REVIEW) {
                        throw new RuntimeException("只有费用待复核状态的申请才能复核");
                    }

                    if (approved) {
                        application.setStatus(ApplicationStatus.COMPLETED);
                    }
                    application.setNeedsReview(false);
                    application.setUpdatedAt(LocalDateTime.now());

                    return dataStore.saveApplication(application);
                })
                .orElseThrow(() -> new RuntimeException("申请不存在"));
    }

    public VehicleApplication cancelApplication(Long applicationId, String reason) {
        return dataStore.findApplicationById(applicationId)
                .map(application -> {
                    if (application.getStatus() == ApplicationStatus.COMPLETED ||
                            application.getStatus() == ApplicationStatus.CANCELLED) {
                        throw new RuntimeException("该申请状态无法取消");
                    }

                    if (application.getAssignedVehicleId() != null) {
                        vehicleService.updateVehicleStatus(application.getAssignedVehicleId(), VehicleStatus.AVAILABLE);
                    }

                    if (application.getAssignedDriverId() != null) {
                        driverService.releaseTask(application.getAssignedDriverId());
                    }

                    application.setStatus(ApplicationStatus.CANCELLED);
                    application.setUpdatedAt(LocalDateTime.now());

                    return dataStore.saveApplication(application);
                })
                .orElseThrow(() -> new RuntimeException("申请不存在"));
    }
}
