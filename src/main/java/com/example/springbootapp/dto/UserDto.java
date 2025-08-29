package com.example.springbootapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for user information.
 * Used for transferring user data between layers.
 */
public class UserDto {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    // ==================== CONSTRUCTORS ====================

    /**
     * Default constructor.
     */
    public UserDto() {
    }

    /**
     * Constructor with name and email.
     * 
     * @param name User's name
     * @param email User's email address
     */
    public UserDto(String name, String email) {
        this.name = name;
        this.email = email;
    }

    /**
     * Constructor with all fields.
     * 
     * @param id User's ID
     * @param name User's name
     * @param email User's email address
     */
    public UserDto(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // ==================== GETTERS AND SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
