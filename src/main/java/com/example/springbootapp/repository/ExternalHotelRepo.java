package com.example.springbootapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.springbootapp.entity.ExternalHotel;
import java.util.Optional;

public interface ExternalHotelRepo extends JpaRepository<ExternalHotel, Long>{

    Optional<ExternalHotel> findByProviderAndExternalHotelId(String provider, String externalHotelId);
    
}
