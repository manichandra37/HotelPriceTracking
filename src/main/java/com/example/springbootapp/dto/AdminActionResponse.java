package com.example.springbootapp.dto;

public record AdminActionResponse(
    Long userId,
    Long ownerId,      // ✅ new
    String name,
    String email,
    String status,
    String message
) {}