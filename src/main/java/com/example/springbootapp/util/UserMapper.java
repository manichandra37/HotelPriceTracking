package com.example.springbootapp.util;

import com.example.springbootapp.dto.UserResponse;
import com.example.springbootapp.entity.User;

public class UserMapper {
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
