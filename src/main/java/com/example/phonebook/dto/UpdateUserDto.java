package com.example.phonebook.dto;

import com.example.phonebook.models.enums.UserRole;

import jakarta.validation.constraints.NotNull;

public class UpdateUserDto {
    private Long id;
    
    @NotNull(message = "Имя пользователя не может быть пустым!")
    private String username;

    @NotNull(message = "Выберите роль!")
    private UserRole role;

    @NotNull(message = "Выберите подразделение!")
    private Long departmentId;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public Long getDepartmentId() {
        return departmentId;
    }
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    public UserRole getRole() {
        return role;
    }
    public void setRole(UserRole role) {
        this.role = role;
    }

}
