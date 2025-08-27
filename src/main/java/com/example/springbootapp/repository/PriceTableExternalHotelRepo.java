package com.example.springbootapp.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.springbootapp.entity.PriceTableExternalHotel;

public interface PriceTableExternalHotelRepo extends JpaRepository<PriceTableExternalHotel, PriceTableExternalHotel.Id> {
    List<PriceTableExternalHotel> findByIdPriceTableId(Long priceTableId);
  }
