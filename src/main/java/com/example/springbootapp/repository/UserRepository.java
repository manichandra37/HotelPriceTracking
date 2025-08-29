package com.example.springbootapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.springbootapp.entity.User;
import com.example.springbootapp.entity.UserStatus;

/**
 * Repository interface for User entity operations.
 * Provides data access methods for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find all users by status.
     * 
     * @param status User status to filter by
     * @return List of users with the specified status
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Check if a user exists with the given email.
     * 
     * @param email Email address to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);
}
