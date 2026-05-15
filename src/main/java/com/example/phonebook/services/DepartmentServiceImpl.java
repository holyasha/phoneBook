 package com.example.phonebook.services;

 import com.example.phonebook.dto.AddDepartmentDto;
 import com.example.phonebook.dto.ShowDepartmentInfoDto;
 import com.example.phonebook.dto.UpdateDepartmentDto;
 import com.example.phonebook.models.entities.Department;
 import com.example.phonebook.repositories.DepartmentRepository;
 import lombok.extern.slf4j.Slf4j;
 import org.modelmapper.ModelMapper;
 import org.springframework.cache.annotation.CacheEvict;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;

 import java.util.List;
 import java.util.stream.Collectors;

 @Slf4j
 @Service
 @Transactional(readOnly = false)
 public class DepartmentServiceImpl implements DepartmentService {
     private final DepartmentRepository departmentRepository;
     private final ModelMapper mapper;

     public DepartmentServiceImpl(DepartmentRepository departmentRepository, ModelMapper mapper) {
         this.departmentRepository = departmentRepository;
         this.mapper = mapper;
         log.info("DepartmentServiceImpl инициализирован");
     }

     @Override
     @Cacheable(value = "departments", key = "'all'")
     public List<ShowDepartmentInfoDto> allDepartments() {
         log.debug("Получение списка всех подразделений");
         List<ShowDepartmentInfoDto> departments = departmentRepository.findAll().stream()
                 .map(department -> mapper.map(department, ShowDepartmentInfoDto.class))
                 .collect(Collectors.toList());
         log.info("Найдено подразделений: {}", departments.size());
         return departments;
     }

     @Override
     public List<ShowDepartmentInfoDto> searchDepartments(String searchTerm) {
         log.debug("Поиск кафедр по запросу: {}", searchTerm);
         List<ShowDepartmentInfoDto> results = departmentRepository.findByShortName(searchTerm).stream()
                 .map(department -> mapper.map(department, ShowDepartmentInfoDto.class))
                 .collect(Collectors.toList());
         log.info("По запросу '{}' найдено подразделений: {}", searchTerm, results.size());
         return results;
     }

     @Override
     @Cacheable(value = "departments", key = "#id")
     public Department getDepartmentById(Long id) {
         log.debug("Получение подразделения по id: {}", id);
         return departmentRepository.findById(id).orElse(null);
     }


     @Override
     @Transactional
     @CacheEvict(cacheNames = "departments", allEntries = true)
     public void updateDepartment(Long id, UpdateDepartmentDto departmentDto) {
         log.debug("Изменение подразделения по id: {}", id);
         departmentRepository.updateById(
                 id,
                 departmentDto.getShortName(),
                 departmentDto.getFullName(),
                 departmentDto.getType()
         );
         log.info("Подразделение с id: {} изменено", id);
     }

     @Override
     @CacheEvict(cacheNames = "departments", allEntries = true)
     public void deleteDepartment(Long id) {
        log.debug("Удаление подразделения с id: {}", id);
        departmentRepository.deleteById(id);
        log.info("Подразделение с id: {} удалено", id);
     }

     @Override
     @CacheEvict(cacheNames = "departments", allEntries = true)
     public void addDepartment(AddDepartmentDto departmentDto) {
         Department department = mapper.map(departmentDto, Department.class);
         departmentRepository.saveAndFlush(department);
     }
 }
