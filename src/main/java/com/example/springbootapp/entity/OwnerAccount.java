package com.example.springbootapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "owner_accounts")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OwnerAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at")
    private Instant createdAt;
}
