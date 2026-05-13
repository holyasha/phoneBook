package com.example.phonebook.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.phonebook.dto.ShowDepartmentInfoDto;
import com.example.phonebook.dto.UpdateUserDto;
import com.example.phonebook.models.entities.UserAccount;
import com.example.phonebook.services.DepartmentService;
import com.example.phonebook.services.UserService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;



@Slf4j
@Controller
@RequestMapping("/moderators")
public class UserController {

    private final UserService userService;
    private final DepartmentService departmentService;

    public UserController(UserService userService, DepartmentService departmentService) {
        this.userService = userService;
        this.departmentService = departmentService;
        log.info("UserController заупущен");
    }

    @GetMapping("/all")
    public String showAllUsers(@RequestParam(required = false) String search,
                                   @RequestParam(required = false) Long department,
                                   Model model) {
        List<ShowDepartmentInfoDto> departments = departmentService.allDepartments();
        model.addAttribute("departments", departments);

        if(search!= null && search.trim().isEmpty()) {
            if (department != null) {
                model.addAttribute("allUsers", userService.searchUsersInDepartment(search, department));
            } else {
                model.addAttribute("allUsers", userService.searchUsers(search));
            }
            model.addAttribute("search", search);
            model.addAttribute("selectedDepartment", department);
            
            if(department != null) {
                String selectedDepartmentName = departments.stream()
                    .filter(d->d.getId().equals(department))
                    .findFirst()
                    .map(ShowDepartmentInfoDto::getShortName)
                    .orElse("Неизвестный отдел");
                model.addAttribute("selectedDepartment", selectedDepartmentName);   
            }
        } else if(department != null) {
                String selectedDepartmentName = departments.stream()
                    .filter(d->d.getId().equals(department))
                    .findFirst()
                    .map(ShowDepartmentInfoDto::getShortName)
                    .orElse("Неизвестный отдел");
                model.addAttribute("selectedDepartment", selectedDepartmentName);   
            } else {
                model.addAttribute("allUsers", userService.allUsers());
            }
        return "moderators-all";        
    }
    
    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        UserAccount user = userService.findUserById(id);

        if(user == null) {
            return "redirect:/moderators/all";
        }

        UpdateUserDto dto = new UpdateUserDto();
        dto.setDepartmentId(user.getDepartment() != null ? user.getDepartment().getId() : null);
        dto.setRole(user.getRole());
        dto.setUsername(user.getUsername());

        model.addAttribute("userModel", dto);
        model.addAttribute("departments", departmentService.allDepartments());
        model.addAttribute("id", id);

        return "moderators-update";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Long id,
        @Valid @ModelAttribute("employeeModel") UpdateUserDto userDto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes) {
        
            if(bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("userModel", userDto);
                redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.employeeModel",
                     bindingResult);
                return "redirect:/moderators/update/" + id;     
            }
        userService.updateUser(id, userDto);    
        return "redirect:/moderators/all";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/moderators/all";
    }
}
