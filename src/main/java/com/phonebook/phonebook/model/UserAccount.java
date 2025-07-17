package com.phonebook.phonebook.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "user_account")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @Column(nullable = false, unique = true)
    @Getter
    @Setter
    private String username;

    @Column(name = "password_hash", nullable = false)
    @Getter
    @Setter
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role",nullable = false)
    @Getter
    @Setter
    private UserRole role;

    @ManyToOne
    @JoinColumn(name = "department_id")
    @Getter
    @Setter
    private Department department;  // NULL для ADMIN

    @Repository
    public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

        // Поиск по username (для авторизации)
        Optional<UserAccount> findByUsername(String username);

        // Поиск всех модераторов определённого подразделения
        List<UserAccount> findByDepartmentIdAndRole(Long departmentId, UserRole role);
    }
}

