package com.example.springbootapp.entity;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.*;

// Minimal entity example
@Entity 
@Table(name="price_table_external_hotels")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class PriceTableExternalHotel {
  @EmbeddedId Id id;
  @Column(name="is_owner_hotel") boolean owner;

  @Embeddable 
  @Data 
  @NoArgsConstructor 
  @AllArgsConstructor
  public static class Id implements Serializable {
    @Column(name="price_table_id") Long priceTableId;
    @Column(name="external_hotel_ref") Long externalHotelRef; // FK -> external_hotels.id
  }
}
