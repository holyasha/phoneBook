package com.example.phonebook.services;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.phonebook.dto.ShowUserDto;
import com.example.phonebook.dto.UpdateUserDto;
import com.example.phonebook.models.entities.UserAccount;
import com.example.phonebook.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper mapper;

    public UserServiceImpl(UserRepository userRepository, 
            ModelMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        log.info("UserServiceImpl инициализирован");
    }

    @Override
    @Cacheable(value = "users", key = "'all'")
    public List<ShowUserDto> allUsers() {
        log.debug("Получение списка всех пользователей");
        List<Long> userIds = userRepository.findAllUserIds();
        List<ShowUserDto> users = userIds.stream()
            .map(this::findUserById)
            .map(user -> mapper.map(user, ShowUserDto.class))
            .collect(Collectors.toList());

        log.debug("Найдено пользователей: {}", users.size());
        return users;
    }

    @Override
    public List<ShowUserDto> searchUsers(String searchTerm) {
        log.debug("Поиск пользователей по запросу: {}");
        List<ShowUserDto> results = userRepository.searchUsers(searchTerm).stream()
            .map(user -> mapper.map(user, ShowUserDto.class))
            .collect(Collectors.toList());   
        log.info("По запросу '{}' найдено пользователей: {}", searchTerm, results.size());
        return results; 
    }

    @Override
    public List<ShowUserDto> searchUsersInDepartment(String searchTerm, Long departmentId) {
        log.debug("Поиск сотрудников в отделе {} по запросу: {}", departmentId, searchTerm);
        List<ShowUserDto> results = userRepository.searchUsersInDepartment(searchTerm, departmentId).stream()
        .map(user -> mapper.map(user, ShowUserDto.class))
        .collect(Collectors.toList());
        return results;  
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "users", allEntries = true)
    public void updateUser(Long id, UpdateUserDto dto) {
        userRepository.updateUserById(
            id,
            dto.getUsername(),
            dto.getDepartmentId(),
            dto.getRole()    
        );
        log.info("Пользователь с id: {} обновлён", id);
    }

    @Override
    @Cacheable(value = "user", key = "#id")
    public UserAccount findUserById(Long id) {
        log.debug("Загрузка пользователя по ID: {}", id);
        return userRepository.findById(id)
            .map(user -> mapper.map(user, UserAccount.class))
            .orElse(null);
    }

    @Override
    @CacheEvict(cacheNames = "users", allEntries = true)
    @Transactional
    @Modifying
    public void deleteUser(Long id) {
        userRepository.deleteUser(id);
    }
}
