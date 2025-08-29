package com.example.springbootapp.dto;


public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        String status
) {}
