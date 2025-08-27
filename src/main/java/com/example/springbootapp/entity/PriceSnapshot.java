package com.example.springbootapp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity 
@Table(name="price_snapshots",
  indexes = {
    @Index(name="k_lookup", columnList="provider,externalHotelId,checkinDate,checkoutDate"),
    @Index(name="k_fetched", columnList="fetchedAt")
})
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class PriceSnapshot {

  @Id 
  @GeneratedValue(strategy=GenerationType.IDENTITY) 
  Long id;

  @Column(nullable=false) 
  String provider;

  @Column(nullable=false) 
  String externalHotelId;
  
  @Column(nullable=false) 
  LocalDate checkinDate;

  @Column(nullable=false) 
  LocalDate checkoutDate;

  @Column(nullable=false) 
  String currency;

  @Column(nullable=false) 
  BigDecimal priceTotal;

  BigDecimal pricePerNight;

  @Column(nullable=false) 
  String availability;  // AVAILABLE | LIMITED | SOLD_OUT

  String source; 
  Instant fetchedAt;
  
  @PrePersist void pre(){ if(fetchedAt==null) fetchedAt=Instant.now(); }

    
}
