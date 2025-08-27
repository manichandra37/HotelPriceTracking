package com.example.springbootapp.dto;

public record AdminActionResponse(
        Long id,
        String name,
        String email,
        String status,
        String message
) {}