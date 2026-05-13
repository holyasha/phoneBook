 package com.example.phonebook.services;

 import com.example.phonebook.dto.AddDepartmentDto;
 import com.example.phonebook.dto.ShowDepartmentInfoDto;
 import com.example.phonebook.dto.UpdateDepartmentDto;
 import com.example.phonebook.models.entities.Department;

 import java.util.List;

 public interface DepartmentService {

    Department getDepartmentById(Long departmentId);
    
    List<ShowDepartmentInfoDto> allDepartments();

    List<ShowDepartmentInfoDto> searchDepartments(String searchTerm);

    void updateDepartment(Long id, UpdateDepartmentDto departmentDto);

    void deleteDepartment(Long id);

    void addDepartment(AddDepartmentDto departmentDto);
 }
