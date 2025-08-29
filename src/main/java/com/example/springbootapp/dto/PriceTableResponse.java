package com.example.springbootapp.dto;

public record PriceTableResponse(
    Long id,
    Long ownerId,
    String name,
    String cityLabel
) {}