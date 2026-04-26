package com.example.fleetmanagement.service;

import com.example.fleetmanagement.model.Employee;
import com.example.fleetmanagement.repository.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private InMemoryDataStore dataStore;

    public List<Employee> getAllEmployees() {
        return dataStore.findAllEmployees();
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return dataStore.findEmployeeById(id);
    }

    public Employee createEmployee(Employee employee) {
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        return dataStore.saveEmployee(employee);
    }

    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        return dataStore.findEmployeeById(id)
                .map(employee -> {
                    employee.setName(updatedEmployee.getName());
                    employee.setDepartment(updatedEmployee.getDepartment());
                    employee.setPhone(updatedEmployee.getPhone());
                    employee.setPosition(updatedEmployee.getPosition());
                    employee.setUpdatedAt(LocalDateTime.now());
                    return dataStore.saveEmployee(employee);
                })
                .orElseThrow(() -> new RuntimeException("员工不存在"));
    }

    public void deleteEmployee(Long id) {
        dataStore.deleteEmployee(id);
    }
}
