package com.phonebook.phonebook.controller;

import com.phonebook.phonebook.model.Employee;
import com.phonebook.phonebook.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*") // Разрешаем запросы из любого источника
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // Получить всех сотрудников
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return ResponseEntity.ok(employees);
    }

    // Создать нового сотрудника
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        Employee savedEmployee = employeeRepository.save(employee);
        return ResponseEntity.ok(savedEmployee);
    }

    // Обновить существующего сотрудника
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable Long id,
            @RequestBody Employee employeeDetails) {

        return employeeRepository.findById(id)
                .map(employee -> {
                    employee.setLastName(employeeDetails.getLastName());
                    employee.setFirstName(employeeDetails.getFirstName());
                    employee.setMiddleName(employeeDetails.getMiddleName());
                    employee.setDepartment(employeeDetails.getDepartment());
                    employee.setWorkPhone(employeeDetails.getWorkPhone());
                    employee.setPersonalPhone(employeeDetails.getPersonalPhone());
                    employee.setEmail(employeeDetails.getEmail());
                    employee.setOfficeNumber(employeeDetails.getOfficeNumber());

                    Employee updatedEmployee = employeeRepository.save(employee);
                    return ResponseEntity.ok(updatedEmployee);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Удалить сотрудника
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employeeRepository.delete(employee);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}