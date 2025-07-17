package com.phonebook.phonebook.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "change_log")
@AllArgsConstructor
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @Column(nullable = false)
    @Getter
    @Setter
    private String action;  // Например: "Создан сотрудник", "Обновлен отдел"

    @Column(nullable = false)
    @Getter
    @Setter
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Getter
    @Setter
    private UserAccount user;

    @Column(columnDefinition = "TEXT")
    @Getter
    @Setter
    private String details;  // Дополнительная информация в JSON

    @Repository
    public interface LogRepository extends JpaRepository<Log, Long> {

        // Поиск логов по пользователю
        List<Log> findByUser(UserAccount user);

        // Поиск логов за определённый период
        List<Log> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    }
}