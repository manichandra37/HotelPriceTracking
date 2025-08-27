package com.example.springbootapp.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.springbootapp.dto.SignupRequest;
import com.example.springbootapp.dto.UserDto;
import com.example.springbootapp.dto.UserResponse;
import com.example.springbootapp.entity.User;
import com.example.springbootapp.entity.UserStatus;
import com.example.springbootapp.repository.UserRepository;
import com.example.springbootapp.service.UserService;
import com.example.springbootapp.util.UserMapper;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository users = null;

    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDto> userDtos = users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(convertToDto(user));
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        User user = convertToEntity(userDto);
        User savedUser = userService.saveUser(user);
        return new ResponseEntity<>(convertToDto(savedUser), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }
        
        existingUser.setName(userDto.getName());
        existingUser.setEmail(userDto.getEmail());
        User updatedUser = userService.saveUser(existingUser);
        return ResponseEntity.ok(convertToDto(updatedUser));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/signup")
public UserResponse signup(@RequestBody @Valid SignupRequest req) {
    if (users.existsByEmail(req.email())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email exists");
    }
    String hash = BCrypt.withDefaults().hashToString(12, req.password().toCharArray());
    User u = User.builder()
            .name(req.name())
            .email(req.email())
            .phone(req.phone())
            .passwordHash(hash)
            .status(UserStatus.PENDING)
            .build();
    users.save(u);
    return UserMapper.toResponse(u);
}

    private void requireAdmin(String key) {
        String expected = System.getenv().getOrDefault("ADMIN_KEY", "dev-admin-key");
        if (!expected.equals(key)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not admin");
        }
    }
    
    // Helper methods for conversion
    private UserDto convertToDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
    
   private User convertToEntity(UserDto userDto) {
       return User.builder()
               .name(userDto.getName())
               .email(userDto.getEmail())
               .status(UserStatus.PENDING)
               .build();
   }
}
