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

    private Long userId;
    private String companyName;
    private boolean isActive;

    @Column(name = "created_at")
    private Instant createdAt;
}
