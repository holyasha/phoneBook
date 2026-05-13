package com.example.phonebook.dto;

import jakarta.validation.constraints.*;

public class AddEmployeeDto {
    private String firstName;
    private String lastName;
    private String middleName;
    private Long departmentId;
    private String officeNumber;
    private String workPhone;
    private String personalPhone;
    private String email;
    private String statusNote;
    private String additionalInfo;


    @NotEmpty(message = "Имя не должно быть пустым!")
    @Size(min = 2, message = "Имя должно содержать не менее 2 символов!")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    @NotEmpty(message = "Фамилия не должна быть пустой!")
    @Size(min = 2, message = "Фамилия должна содержать не менее 2 символов!")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    @NotNull(message = "Выберите подразделение!")
    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    @NotEmpty(message = "Номер кабинета не может быть пустым")
    @Size(min = 4, message = "Номер кабинета должен быть не менее 4 символов")
    public String getOfficeNumber() {
        return officeNumber;
    }

    public void setOfficeNumber(String officeNumber) {
        this.officeNumber = officeNumber;
    }
    @NotEmpty(message = "Рабочий телефон не должен быть пустым")
    @Size(max = 4, message = "Вписывайте тольько добавочный номер")
    public String getWorkPhone() {
        return workPhone;
    }

    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
    }
    @NotEmpty(message = "номер телефона не должен быть пустым")
    public String getPersonalPhone() {
        return personalPhone;
    }

    public void setPersonalPhone(String personalPhone) {
        this.personalPhone = personalPhone;
    }
    @Email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

   public String getStatusNote() { return statusNote; }
    public void setStatusNote(String statusNote) { this.statusNote = (statusNote != null && statusNote.trim().isEmpty()) ? null : statusNote; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = (additionalInfo != null && additionalInfo.trim().isEmpty()) ? null : additionalInfo; }
}
