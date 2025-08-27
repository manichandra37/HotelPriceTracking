package com.example.springbootapp.controller;

import com.example.springbootapp.dto.UserDto;
import com.example.springbootapp.entity.User;
import com.example.springbootapp.entity.UserStatus;
import com.example.springbootapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
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
