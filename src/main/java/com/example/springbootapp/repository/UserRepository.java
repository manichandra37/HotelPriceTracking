package com.example.springbootapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.springbootapp.entity.User;
import com.example.springbootapp.entity.UserStatus;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByStatus(UserStatus status);
  boolean existsByEmail(String email);
}
