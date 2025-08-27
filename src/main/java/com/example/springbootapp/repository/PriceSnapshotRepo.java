package com.example.springbootapp.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.springbootapp.entity.PriceSnapshot;

public interface PriceSnapshotRepo extends JpaRepository<PriceSnapshot, Long>{

    List<PriceSnapshot> findByProviderAndExternalHotelIdAndCheckinDateAndCheckoutDate(
      String provider, String externalHotelId, LocalDate checkinDate, LocalDate checkoutDate);

      @Query("""
    select s from PriceSnapshot s
     where s.provider=:provider and s.externalHotelId=:hid
       and s.checkinDate=:cin and s.checkoutDate=:cout
       and s.fetchedAt >= :freshAfter
     order by s.fetchedAt desc
  """)
  List<PriceSnapshot> findFresh(
      @Param("provider") String provider,
      @Param("hid") String hotelId,
      @Param("cin") LocalDate checkin,
      @Param("cout") LocalDate checkout,
      @Param("freshAfter") Instant freshAfter);

      List<PriceSnapshot> findByProviderAndExternalHotelIdAndCheckinDateGreaterThanEqualAndCheckoutDateLessThanEqualOrderByCheckinDateAsc(
      String provider,
      String externalHotelId,
      LocalDate checkinFrom,
      LocalDate checkoutTo
  );
}
