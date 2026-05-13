package com.example.phonebook.services;

import java.util.List;


import com.example.phonebook.dto.AddEmployeeDto;
import com.example.phonebook.dto.ShowEmployeeDto;
import com.example.phonebook.dto.UpdateEmployeeDto;
import com.example.phonebook.models.entities.Employee;
import com.example.phonebook.models.entities.UserAccount;

public interface EmployeeService {
    
    void addEmployee(AddEmployeeDto employeeDto);

    List<ShowEmployeeDto> allEmployees();

    List<ShowEmployeeDto> allInactives();

    List<ShowEmployeeDto> searchEmployees(String searchTerm);

    Employee findById(Long id);
    
    List<ShowEmployeeDto> findEmployeesByDepartment(Long departmentId);

    void updateEmployee(Long id, UpdateEmployeeDto dto);
    
    List<ShowEmployeeDto> searchEmployeesInDepartment(String searchTerm, Long departmentId);

    void fireEmployee(Long id);

    UserAccount getCurrentUser();

    Long getCurrentUserDepartmentId();

    boolean isCurrentUserAdmin();

    boolean isCurrentUserModerator();

    void makeActive(Long id);
}
