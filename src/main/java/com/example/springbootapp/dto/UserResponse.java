package com.example.springbootapp.dto;

import lombok.Data;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        String status
) {}
