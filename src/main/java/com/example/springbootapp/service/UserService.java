package com.example.springbootapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.springbootapp.entity.User;
import com.example.springbootapp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service class for user management operations.
 * Handles business logic for user CRUD operations.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // ==================== USER OPERATIONS ====================

    /**
     * Get all users.
     * 
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get user by ID.
     * 
     * @param id User ID
     * @return User if found, null otherwise
     */
    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    /**
     * Save or update a user.
     * 
     * @param user User to save
     * @return Saved user
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Delete a user by ID.
     * 
     * @param id User ID to delete
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
