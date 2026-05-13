package com.example.phonebook.repositories;

import com.example.phonebook.models.entities.UserAccount;
import com.example.phonebook.models.enums.UserRole;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);

    @Query("SELECT u FROM UserAccount u ORDER BY u.username ASC")
    List<UserAccount> findAllOrderedByLastName();
    
    @Modifying
    @Transactional
    @Query("""
    UPDATE UserAccount u SET
        u.username = :username,
        u.department.id = :departmentId,
        u.role = :role
    WHERE u.id = :id
""")
    void updateUserById(
            @Param("id") Long id,
            @Param("username") String username,
            @Param("departmentId") Long departmentId,
            @Param("role") UserRole role
    );

    @Query("SELECT u.id FROM UserAccount u WHERE " +
           "u.role IN ('USER', 'MODERATOR') " +
           "ORDER BY username ASC")
    List<Long> findAllUserIds();

    @Query("SELECT u FROM UserAccount u WHERE " +
           "CAST(u.id AS string) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND " +
           "u.role IN ('USER', 'MODERATOR') " +
            "ORDER BY u.username ASC")
    List<UserAccount> searchUsers(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT u FROM UserAccount u WHERE " +
       "u.department.id = :departmentId AND (" +
       "CAST(u.id AS string) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(u.role) LIKE LOWER(CONCAT('%', :searchTerm, '%')))" +
        "ORDER BY u.username ASC")
    List<UserAccount> searchUsersInDepartment( @Param("searchTerm") String searchTerm, @Param("departmentId") Long departmentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserAccount u WHERE u.id = :id")
    void deleteUser(@Param("id") Long id);
}
