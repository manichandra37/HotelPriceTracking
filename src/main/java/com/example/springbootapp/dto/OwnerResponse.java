package com.example.springbootapp.dto;

public record OwnerResponse(
    Long ownerId,
    Long userId,
    String companyName,
    boolean active
) {}
