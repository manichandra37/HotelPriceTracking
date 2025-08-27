package com.example.springbootapp.repository;

import com.example.springbootapp.entity.OwnerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerAccountRepo extends JpaRepository<OwnerAccount, Long> {
    
}