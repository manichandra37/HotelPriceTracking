package com.example.springbootapp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "external_hotels",uniqueConstraints = @UniqueConstraint(name="u_provider_ext", columnNames={"provider","external_hotel_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalHotel {

  @Id 
  @GeneratedValue(strategy = GenerationType.IDENTITY) 
  Long id;
  
  @Column(nullable=false) 
  String provider;          // e.g. RAPIDAPI_BOOKING

  @Column(name="external_hotel_id", nullable=false) 
  String externalHotelId;

  String nameCached; 
  String urlCached;
  String addressCached; 
  String cityCached; 
  String stateCached; 
  String countryCached;
  Boolean isActive; 
  Instant lastSeenAt; 
  Instant createdAt; 
  Instant updatedAt;

  @PrePersist void preP(){createdAt=Instant.now();}
  @PreUpdate  void preU(){updatedAt=Instant.now();}
    
}
