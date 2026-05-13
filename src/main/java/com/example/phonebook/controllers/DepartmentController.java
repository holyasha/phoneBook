package com.example.phonebook.controllers;

import com.example.phonebook.dto.AddDepartmentDto;
import com.example.phonebook.dto.ShowDepartmentInfoDto;
import com.example.phonebook.dto.UpdateDepartmentDto;
import com.example.phonebook.models.entities.Department;
import com.example.phonebook.services.DepartmentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/add")
    public String addDepartmentForm(Model model) {
        AddDepartmentDto departmentDto = new AddDepartmentDto();
        model.addAttribute("departmentModel", departmentDto);
        return "department-add";
    }

    @PostMapping("/add")
    public String addDepartments(
            @Valid @ModelAttribute("departmentModel") AddDepartmentDto departmentDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("departmentModel", departmentDto);
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.departmentModel",
                    bindingResult
            );
            return "redirect:/departments/add";
        }
        departmentService.addDepartment(departmentDto);
        return "redirect:/employees/all";
    }

    @GetMapping("/all")
    public String showAllDepartments(Model model) {
        List<ShowDepartmentInfoDto> departments = departmentService.allDepartments();
        model.addAttribute("allDepartments", departments);
        return "department-all";
    }

    @GetMapping("/update/{id}")
    public String updateDepartmentForm(
            @PathVariable Long id,
            Model model
    ) {
        Department department = departmentService.getDepartmentById(id);

        if(department == null) {
            return "redirect:/moderators/all";
        }

        UpdateDepartmentDto dto = new UpdateDepartmentDto();
        dto.setShortName(department.getShortName());
        dto.setFullName(department.getFullName());
        dto.setType(department.getType());

        model.addAttribute("departmentModel", dto);
        model.addAttribute("id", id);
        return "department-update";
    }

    @PostMapping("/update/{id}")
    public String updateDepartment(
            @PathVariable Long id,
            @Valid @ModelAttribute("departmentModel") UpdateDepartmentDto departmentDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if(bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("departmentModel", departmentDto);
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.departmentModel",
                    bindingResult);
            return "redirect:/departments/update/" + id;
        }
        departmentService.updateDepartment(id, departmentDto);
        return "redirect:/departments/all";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            departmentService.deleteDepartment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Подразделение успешно удалено!");
        } catch (Exception e) {
            log.error("Ошибка при удалении отдела", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/departments/all";
    }
}
