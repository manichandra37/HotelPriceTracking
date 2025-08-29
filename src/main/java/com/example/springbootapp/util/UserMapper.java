package com.example.springbootapp.util;

import com.example.springbootapp.dto.UserResponse;
import com.example.springbootapp.entity.User;

/**
 * Utility class for mapping between User entities and DTOs.
 * Provides static methods for entity-to-DTO conversions.
 */
public class UserMapper {

    /**
     * Convert User entity to UserResponse DTO.
     * 
     * @param u User entity to convert
     * @return UserResponse DTO
     */
    public static UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getPhone(),
                u.getStatus().name()
        );
    }
}
