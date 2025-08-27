package com.example.springbootapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.springbootapp.dto.AdminActionResponse;
import com.example.springbootapp.dto.OwnerResponse;
import com.example.springbootapp.dto.UserResponse;
import com.example.springbootapp.entity.OwnerAccount;
import com.example.springbootapp.entity.UserStatus;
import com.example.springbootapp.repository.OwnerAccountRepo;
import com.example.springbootapp.repository.UserRepository;
import com.example.springbootapp.util.UserMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserRepository users ;
    private final OwnerAccountRepo ownerAccounts ;

    @GetMapping("/users/pending")
public List<UserResponse> pending(@RequestHeader("X-ADMIN-KEY") String key) {
    requireAdmin(key);
    return users.findByStatus(UserStatus.PENDING)
                .stream()
                .map(UserMapper::toResponse)
                .toList();
}

@PostMapping("/users/{id}/approve")
public AdminActionResponse approve(
        @RequestHeader("X-ADMIN-KEY") String key,
        @PathVariable Long id) {

    requireAdmin(key);

    var u = users.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    u.setStatus(UserStatus.APPROVED);
    users.save(u);

    // ✅ Create OwnerAccount when user approved
    OwnerAccount account = OwnerAccount.builder()
            .userId(u.getId())
            .companyName(u.getName() + " Hotels") // default, can be edited later
            .active(true)
            .build();
    ownerAccounts.save(account);

    return new AdminActionResponse(
      u.getId(),
      account.getId(),          // return ownerId
      u.getName(),
      u.getEmail(),
      u.getStatus().name(),
      "User approved and owner account created"
  );
}

@GetMapping("/owners")
public List<OwnerResponse> owners(@RequestHeader("X-ADMIN-KEY") String key) {
  requireAdmin(key);
  return ownerAccounts.findAll().stream()
      .map(o -> new OwnerResponse(o.getId(), o.getUserId(), o.getCompanyName(), o.isActive()))
      .toList();
}

private void requireAdmin(String key) {
    String expected = System.getenv().getOrDefault("ADMIN_KEY", "dev-admin-key");
    if (!expected.equals(key)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not admin");
    }
}

}
