package com.example.springbootapp.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import com.example.springbootapp.repository.ExternalHotelRepo;
import com.example.springbootapp.repository.PriceSnapshotRepo;

import lombok.RequiredArgsConstructor;

import com.example.springbootapp.entity.ExternalHotel;
import com.example.springbootapp.entity.PriceSnapshot;

@Service
@RequiredArgsConstructor
public class PricingIngestService {

    private final ExternalHotelRepo hotels;
  private final PriceSnapshotRepo snapshots;

  public ExternalHotel upsertHotel(String provider, String extId,
                                   String name, String url,
                                   String city, String state, String country,
                                   boolean active) {
    var h = hotels.findByProviderAndExternalHotelId(provider, extId)
        .orElseGet(() -> ExternalHotel.builder()
            .provider(provider).externalHotelId(extId).build());
    h.setNameCached(name); h.setUrlCached(url);
    h.setCityCached(city); h.setStateCached(state); h.setCountryCached(country);
    h.setIsActive(active); h.setLastSeenAt(java.time.Instant.now());
    return hotels.save(h);
  }

  public PriceSnapshot insertSnapshot(String provider, String extId,
                                      LocalDate checkin, LocalDate checkout,
                                      String currency, BigDecimal total, BigDecimal perNight,
                                      String availability) {
    var s = PriceSnapshot.builder()
        .provider(provider).externalHotelId(extId)
        .checkinDate(checkin).checkoutDate(checkout)
        .currency(currency).priceTotal(total).pricePerNight(perNight)
        .availability(availability).source("RAPIDAPI").build();
    return snapshots.save(s);
  }
    
}
