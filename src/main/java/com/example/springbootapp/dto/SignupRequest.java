package com.example.springbootapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        String phone,
        @NotBlank String password
) {}