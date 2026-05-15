package com.example.phonebook.controllers;

import com.example.phonebook.dto.AddEmployeeDto;
import com.example.phonebook.dto.ShowDepartmentInfoDto;
import com.example.phonebook.dto.UpdateEmployeeDto;
import com.example.phonebook.models.entities.Employee;
import com.example.phonebook.services.DepartmentService;
import com.example.phonebook.services.EmployeeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    public EmployeeController(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        log.info("EmployeeController инициализирован");
    }


    @GetMapping("/add")
    public String addEmployeeForm(Model model) {
        if (!model.containsAttribute("employeeModel")) {
            boolean isModerator = employeeService.isCurrentUserModerator();
            Long userDepartmentId = employeeService.getCurrentUserDepartmentId();
            AddEmployeeDto addEmployeeDto = new AddEmployeeDto();
            if (isModerator) {
                addEmployeeDto.setDepartmentId(userDepartmentId);
            }
            model.addAttribute("isModerator", isModerator);
            model.addAttribute("userDepartmentId", userDepartmentId);
            model.addAttribute("employeeModel", addEmployeeDto);
        }

        model.addAttribute("departments", departmentService.allDepartments());
        return "employee-add";
    }

    @PostMapping("/add")
    public String addEmployee(
            @Valid @ModelAttribute("employeeModel") AddEmployeeDto employeeModel,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("employeeModel", employeeModel);
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.employeeModel",
                    bindingResult
            );
            return "redirect:/employees/add";
        }
        employeeService.addEmployee(employeeModel);
        return "redirect:/employees/all";
    }


    @GetMapping("/all")
    public String showAllEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long department,
            Model model) {

        log.debug("Отображение списка сотрудников: страница {}, размер {}, сортировка {}, поиск {}");
        List<ShowDepartmentInfoDto> departments = departmentService.allDepartments();
        model.addAttribute("departments", departments);
        boolean isAdmin = employeeService.isCurrentUserAdmin();
        boolean isModerator = employeeService.isCurrentUserModerator();
        Long userDepartmentId = employeeService.getCurrentUserDepartmentId();

        log.debug("Права пользователя: admin={}, moderator={}, departmentId={}",
                isAdmin, isModerator, userDepartmentId);

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isModerator", isModerator);
        model.addAttribute("userDepartmentId", userDepartmentId);
        if (search != null && !search.trim().isEmpty()) {
            if (department != null) {
                model.addAttribute("allEmployees", employeeService.searchEmployeesInDepartment(search, department));
            } else {
                model.addAttribute("allEmployees", employeeService.searchEmployees(search));
            }
            model.addAttribute("search", search);
            model.addAttribute("selectedDepartment", department);

            if (department != null) {
                String selectedDepartmentName = departments.stream()
                        .filter(d -> d.getId().equals(department))
                        .findFirst()
                        .map(ShowDepartmentInfoDto::getShortName)
                        .orElse("Неизвестное подразделение");
                model.addAttribute("selectedDepartmentName", selectedDepartmentName);
            }

        } else if (department != null) {
            model.addAttribute("allEmployees", employeeService.findEmployeesByDepartment(department));
            model.addAttribute("selectedDepartment", department);

            String selectedDepartmentName = departments.stream()
                    .filter(d -> d.getId().equals(department))
                    .findFirst()
                    .map(ShowDepartmentInfoDto::getShortName)
                    .orElse("Неизвестное подразделение");
            model.addAttribute("selectedDepartmentName", selectedDepartmentName);

        } else {
            model.addAttribute("allEmployees", employeeService.searchEmployees(search));
            model.addAttribute("search", search);
            model.addAttribute("selectedDepartment", department);
        }

        return "employee-all";
    }


    @PostMapping("/delete/{id}")
    public String fireEmployee(@PathVariable("id") Long id) {
        employeeService.fireEmployee(id);
        return "redirect:/employees/all";
    }


    @GetMapping("/update/{id}")
    public String showUpdateForm(
            @PathVariable Long id,
            Model model
    ) {
        Employee employee = employeeService.findById(id);
        if (employee == null || !employee.IsActive()) {
            return "redirect:/employees/all?error=Сотрудник не найден";
        }

        UpdateEmployeeDto dto = new UpdateEmployeeDto();
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setMiddleName(employee.getMiddleName());
        dto.setDepartmentId(employee.getDepartment().getId());
        dto.setOfficeNumber(employee.getOfficeNumber());
        dto.setWorkPhone(employee.getWorkPhone());
        dto.setPersonalPhone(employee.getPersonalPhone());
        dto.setEmail(employee.getEmail());
        dto.setStatusNote(employee.getStatusNote());
        dto.setAdditionalInfo(employee.getAdditionalInfo());

        model.addAttribute("employeeModel", dto);
        model.addAttribute("departments", departmentService.allDepartments());
        model.addAttribute("id", id);

        return "employee-update";
    }


    @PostMapping("/update/{id}")
    public String updateEmployee(
            @PathVariable Long id,
            @Valid @ModelAttribute("employeeModel") UpdateEmployeeDto employeeDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("employeeModel", employeeDto);
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.employeeModel",
                    bindingResult
            );
            return "redirect:/employees/update/" + id;
        }

        employeeService.updateEmployee(id, employeeDto);

        return "redirect:/employees/all";
    }

    @GetMapping("/inactive")
    public String showInactiveEmployees(
            Model model) {

            model.addAttribute("allEmployees", employeeService.allInactives());

        return "employee-inactive";
    }

    @PostMapping("/makeActive/{id}")
    public String makeActive(@PathVariable Long id) {
        employeeService.makeActive(id);
        return "redirect:/employees/inactive";
    }
}
