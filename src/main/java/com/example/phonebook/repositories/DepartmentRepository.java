package com.example.phonebook.repositories;

import com.example.phonebook.models.entities.Department;
import com.example.phonebook.models.enums.DepartmentType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Query("SELECT d FROM Department d ORDER BY d.shortName")
    List<Department> findAll();

    List<Department> findByShortName(String shortName);

    boolean existsByShortName(String shortName);

    @Query("SELECT d FROM Department d WHERE d.id = :id")
    Optional<Department> findById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("DELETE FROM Department d WHERE d.id = :id")
    void deleteById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Department d SET " +
            "d.shortName = :shortName, " +
            "d.fullName = :fullName, " +
            "d.type = :type " +
            "WHERE d.id = :id")
    void updateById(
            @Param("id") Long id,
            @Param("shortName") String ShortName,
            @Param("fullName") String fullName,
            @Param("type")DepartmentType type
            );
}