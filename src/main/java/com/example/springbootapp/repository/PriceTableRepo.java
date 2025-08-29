package com.example.springbootapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.springbootapp.entity.PriceTable;

public interface PriceTableRepo extends JpaRepository<PriceTable, Long> {

    List<PriceTable> findByOwnerId(Long ownerId);
}
