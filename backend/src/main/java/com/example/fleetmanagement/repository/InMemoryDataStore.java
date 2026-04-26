package com.example.fleetmanagement.repository;

import com.example.fleetmanagement.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryDataStore {

    private final Map<Long, Vehicle> vehicles = new ConcurrentHashMap<>();
    private final Map<Long, Driver> drivers = new ConcurrentHashMap<>();
    private final Map<Long, Employee> employees = new ConcurrentHashMap<>();
    private final Map<Long, VehicleApplication> applications = new ConcurrentHashMap<>();
    private final Map<Long, MaintenanceOrder> maintenanceOrders = new ConcurrentHashMap<>();
    private final Map<Long, MonthlyReport> monthlyReports = new ConcurrentHashMap<>();

    private final AtomicLong vehicleIdGenerator = new AtomicLong(1);
    private final AtomicLong driverIdGenerator = new AtomicLong(1);
    private final AtomicLong employeeIdGenerator = new AtomicLong(1);
    private final AtomicLong applicationIdGenerator = new AtomicLong(1);
    private final AtomicLong maintenanceIdGenerator = new AtomicLong(1);
    private final AtomicLong reportIdGenerator = new AtomicLong(1);

    public InMemoryDataStore() {
        initializeSampleData();
    }

    private void initializeSampleData() {
        // 初始化示例车辆数据
        vehicles.put(vehicleIdGenerator.get(), new Vehicle(
                vehicleIdGenerator.getAndIncrement(),
                "京A12345",
                VehicleType.SEDAN,
                5,
                VehicleStatus.AVAILABLE,
                15000.0,
                5000.0,
                10000.0,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        vehicles.put(vehicleIdGenerator.get(), new Vehicle(
                vehicleIdGenerator.getAndIncrement(),
                "京B67890",
                VehicleType.SUV,
                7,
                VehicleStatus.AVAILABLE,
                25000.0,
                5000.0,
                20000.0,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        vehicles.put(vehicleIdGenerator.get(), new Vehicle(
                vehicleIdGenerator.getAndIncrement(),
                "京C11111",
                VehicleType.VAN,
                9,
                VehicleStatus.AVAILABLE,
                35000.0,
                5000.0,
                30000.0,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        vehicles.put(vehicleIdGenerator.get(), new Vehicle(
                vehicleIdGenerator.getAndIncrement(),
                "京D22222",
                VehicleType.BUS,
                45,
                VehicleStatus.AVAILABLE,
                80000.0,
                10000.0,
                70000.0,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        // 初始化示例司机数据
        drivers.put(driverIdGenerator.get(), new Driver(
                driverIdGenerator.getAndIncrement(),
                "张三",
                "13800138001",
                "A12345678",
                Arrays.asList(VehicleType.SEDAN, VehicleType.SUV, VehicleType.VAN),
                DriverStatus.AVAILABLE,
                0,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        drivers.put(driverIdGenerator.get(), new Driver(
                driverIdGenerator.getAndIncrement(),
                "李四",
                "13800138002",
                "B87654321",
                Arrays.asList(VehicleType.SEDAN, VehicleType.SUV),
                DriverStatus.AVAILABLE,
                0,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        drivers.put(driverIdGenerator.get(), new Driver(
                driverIdGenerator.getAndIncrement(),
                "王五",
                "13800138003",
                "C11223344",
                Arrays.asList(VehicleType.SEDAN, VehicleType.SUV, VehicleType.VAN, VehicleType.BUS),
                DriverStatus.AVAILABLE,
                0,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        // 初始化示例员工数据
        employees.put(employeeIdGenerator.get(), new Employee(
                employeeIdGenerator.getAndIncrement(),
                "赵六",
                "市场部",
                "13900139001",
                "经理",
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        employees.put(employeeIdGenerator.get(), new Employee(
                employeeIdGenerator.getAndIncrement(),
                "钱七",
                "技术部",
                "13900139002",
                "工程师",
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        employees.put(employeeIdGenerator.get(), new Employee(
                employeeIdGenerator.getAndIncrement(),
                "孙八",
                "人事部",
                "13900139003",
                "主管",
                LocalDateTime.now(),
                LocalDateTime.now()
        ));
    }

    // 车辆相关操作
    public Vehicle saveVehicle(Vehicle vehicle) {
        if (vehicle.getId() == null) {
            vehicle.setId(vehicleIdGenerator.getAndIncrement());
            vehicle.setCreatedAt(LocalDateTime.now());
        }
        vehicle.setUpdatedAt(LocalDateTime.now());
        vehicles.put(vehicle.getId(), vehicle);
        return vehicle;
    }

    public Optional<Vehicle> findVehicleById(Long id) {
        return Optional.ofNullable(vehicles.get(id));
    }

    public List<Vehicle> findAllVehicles() {
        return new ArrayList<>(vehicles.values());
    }

    public void deleteVehicle(Long id) {
        vehicles.remove(id);
    }

    // 司机相关操作
    public Driver saveDriver(Driver driver) {
        if (driver.getId() == null) {
            driver.setId(driverIdGenerator.getAndIncrement());
            driver.setCreatedAt(LocalDateTime.now());
        }
        driver.setUpdatedAt(LocalDateTime.now());
        drivers.put(driver.getId(), driver);
        return driver;
    }

    public Optional<Driver> findDriverById(Long id) {
        return Optional.ofNullable(drivers.get(id));
    }

    public List<Driver> findAllDrivers() {
        return new ArrayList<>(drivers.values());
    }

    public void deleteDriver(Long id) {
        drivers.remove(id);
    }

    // 员工相关操作
    public Employee saveEmployee(Employee employee) {
        if (employee.getId() == null) {
            employee.setId(employeeIdGenerator.getAndIncrement());
            employee.setCreatedAt(LocalDateTime.now());
        }
        employee.setUpdatedAt(LocalDateTime.now());
        employees.put(employee.getId(), employee);
        return employee;
    }

    public Optional<Employee> findEmployeeById(Long id) {
        return Optional.ofNullable(employees.get(id));
    }

    public List<Employee> findAllEmployees() {
        return new ArrayList<>(employees.values());
    }

    public void deleteEmployee(Long id) {
        employees.remove(id);
    }

    // 用车申请相关操作
    public VehicleApplication saveApplication(VehicleApplication application) {
        if (application.getId() == null) {
            application.setId(applicationIdGenerator.getAndIncrement());
            application.setCreatedAt(LocalDateTime.now());
        }
        application.setUpdatedAt(LocalDateTime.now());
        applications.put(application.getId(), application);
        return application;
    }

    public Optional<VehicleApplication> findApplicationById(Long id) {
        return Optional.ofNullable(applications.get(id));
    }

    public List<VehicleApplication> findAllApplications() {
        return new ArrayList<>(applications.values());
    }

    public void deleteApplication(Long id) {
        applications.remove(id);
    }

    // 维修保养工单相关操作
    public MaintenanceOrder saveMaintenanceOrder(MaintenanceOrder order) {
        if (order.getId() == null) {
            order.setId(maintenanceIdGenerator.getAndIncrement());
            order.setCreatedAt(LocalDateTime.now());
        }
        order.setUpdatedAt(LocalDateTime.now());
        maintenanceOrders.put(order.getId(), order);
        return order;
    }

    public Optional<MaintenanceOrder> findMaintenanceOrderById(Long id) {
        return Optional.ofNullable(maintenanceOrders.get(id));
    }

    public List<MaintenanceOrder> findAllMaintenanceOrders() {
        return new ArrayList<>(maintenanceOrders.values());
    }

    public void deleteMaintenanceOrder(Long id) {
        maintenanceOrders.remove(id);
    }

    // 月度报表相关操作
    public MonthlyReport saveMonthlyReport(MonthlyReport report) {
        if (report.getId() == null) {
            report.setId(reportIdGenerator.getAndIncrement());
        }
        monthlyReports.put(report.getId(), report);
        return report;
    }

    public Optional<MonthlyReport> findMonthlyReportById(Long id) {
        return Optional.ofNullable(monthlyReports.get(id));
    }

    public List<MonthlyReport> findAllMonthlyReports() {
        return new ArrayList<>(monthlyReports.values());
    }
}
