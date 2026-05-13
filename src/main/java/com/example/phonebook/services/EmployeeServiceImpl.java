package com.example.phonebook.services;

import java.util.List;
import java.util.stream.Collectors;

import com.example.phonebook.dto.UpdateEmployeeDto;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.phonebook.dto.AddEmployeeDto;
import com.example.phonebook.dto.ShowEmployeeDto;
import com.example.phonebook.models.entities.Department;
import com.example.phonebook.models.entities.Employee;
import com.example.phonebook.models.entities.UserAccount;
import com.example.phonebook.models.enums.UserRole;
import com.example.phonebook.repositories.DepartmentRepository;
import com.example.phonebook.repositories.EmployeeRepository;
import com.example.phonebook.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final ModelMapper mapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, UserRepository userRepository, ModelMapper mapper) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        log.info("EmployeeServiceImpl инициализорован");
    }
    
    @Override
    @Transactional
    @CacheEvict(cacheNames = "employees", allEntries = true)
    public void addEmployee(AddEmployeeDto employeeDto) {
        log.debug("Добавление нового сотрудника: {} {} {}", employeeDto.getLastName(), employeeDto.getFirstName(), employeeDto.getMiddleName());

        Department department = departmentRepository.findById(employeeDto.getDepartmentId()).orElse(null);
        if (department == null) {
            log.warn("Попытка добавить сотрудника в несуществующий отдел");
            //throw new EmployeeNotFoundException("Сотрудник с именем '" + employeeFullName + "' не найден");
        }
        Employee employee = mapper.map(employeeDto, Employee.class);
        employee.setDepartment(department);
        employee.setActive(true);

        employeeRepository.saveAndFlush(employee);
        log.info("Сотрудник успешно добавлен: {} {} {}", employeeDto.getLastName(), employeeDto.getMiddleName(), employeeDto.getFirstName());
    }

    @Override
    @Cacheable(value = "employees", key = "'all'")
    public List<ShowEmployeeDto> allEmployees() {
        log.debug("Получение списка всех сотрудников");
        List<ShowEmployeeDto> employees = employeeRepository.findAllActiveOrdered().stream()
                .map(employee -> mapper.map(employee, ShowEmployeeDto.class))
                .collect(Collectors.toList());
        log.debug("Найдено сотрудников: {}", employees.size());
        return employees;
    }

    @Override
    public List<ShowEmployeeDto> findEmployeesByDepartment(Long departmentId) {
        log.debug("Получение сотрудников отдела: {}", departmentId);
        List<ShowEmployeeDto> employees = employeeRepository.findByDepartmentId(departmentId).stream()
            .map(employee -> mapper.map(employee, ShowEmployeeDto.class))
            .collect(Collectors.toList());
        log.debug("Найдено сотрудников в отделе: {}", employees.size());
    return employees;
}

    @Override
    @Transactional
    @CacheEvict(cacheNames = "employees", allEntries = true)
    public void fireEmployee(Long id) {
        log.debug("Увольнение сотрудника: {}", id);

                employeeRepository.findEmployeeById(id)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));

        employeeRepository.deactivateById(id);
        log.info("Сотрудник уволен: {}", id);
    }

    @Override
    public List<ShowEmployeeDto> searchEmployeesInDepartment(String searchTerm, Long departmentId) {
        log.debug("Поиск сотрудников в отделе {} по запросу: {}", departmentId, searchTerm);
        List<ShowEmployeeDto> results = employeeRepository.searchActiveEmployeesInDepartment(searchTerm, departmentId).stream()
            .map(employee -> mapper.map(employee, ShowEmployeeDto.class))
            .collect(Collectors.toList());
        log.info("По запросу '{}' в отделе {} найдено сотрудников: {}", searchTerm, departmentId, results.size());        
        return results;
    }

    @Override
    public List<ShowEmployeeDto> searchEmployees(String searchTerm) {
        log.debug("Поиск сотрудников по запросу: {}", searchTerm);
        List<ShowEmployeeDto> results = employeeRepository.searchActiveEmployees(searchTerm).stream()
                .map(employee -> mapper.map(employee, ShowEmployeeDto.class))
                .collect(Collectors.toList());
        log.info("По запросу '{}' найдено сотрудников: {}", searchTerm, results.size());        
        return results;
    }

    @Override
    public Employee findById(Long id) {
        log.debug("Поиск сотрудника по ID: {}", id);
        return employeeRepository.findEmployeeById(id).orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "employees", allEntries = true)
    public void updateEmployee(Long id, UpdateEmployeeDto dto) {
        
        employeeRepository.updateEmployeeById(
                id,
                dto.getFirstName(),
                dto.getLastName(),
                dto.getMiddleName(),
                dto.getDepartmentId(),
                dto.getOfficeNumber(),
                dto.getWorkPhone(),
                dto.getPersonalPhone(),
                dto.getEmail(),
                dto.getStatusNote(),
                dto.getAdditionalInfo()
        );

        log.info("Сотрудник обновлён: {}", id);
    }

    @Override 
    public UserAccount getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public Long getCurrentUserDepartmentId() {
        UserAccount user = getCurrentUser();
        return (user != null && user.getDepartment() != null) 
                ? user.getDepartment().getId() 
                : null;
    }

    @Override
    public boolean isCurrentUserAdmin() {
        UserAccount user = getCurrentUser();
        return user != null && user.getRole() == UserRole.ADMIN;
    }
    
    @Override
    public boolean isCurrentUserModerator() {
        UserAccount user = getCurrentUser();
        return user != null && user.getRole() == UserRole.MODERATOR;
    }

    @Override
    public List<ShowEmployeeDto> allInactives() {
        log.debug("Получение списка неактивных сотрудников");
        List<ShowEmployeeDto> employees = employeeRepository.findAllInactiveOrdered().stream()
                .map(employee -> mapper.map(employee, ShowEmployeeDto.class))
                .collect(Collectors.toList());
        log.debug("Найдено неактивных сотрудников: {}", employees.size());
        return employees;
    }

    @Override
    @Transactional
    @Modifying
    @CacheEvict(cacheNames = "employees", allEntries = true)
    public void makeActive(Long id) {
        employeeRepository.makeActive(id);
    }

}