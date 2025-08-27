package com.example.springbootapp.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.springbootapp.dto.NightlySimpleRow;
import com.example.springbootapp.dto.PriceRow;
import com.example.springbootapp.entity.PriceSnapshot;
import com.example.springbootapp.repository.ExternalHotelRepo;
import com.example.springbootapp.repository.PriceSnapshotRepo;

@Service
public class UiPriceService {

    @Autowired
    private final PriceSnapshotRepo snapshots;
    
    @Autowired
    private final ExternalHotelRepo hotels;

    public UiPriceService(PriceSnapshotRepo snapshots, ExternalHotelRepo hotels) {
        this.snapshots = snapshots;
        this.hotels = hotels;
    }

    public PriceRow singleDay(String provider, String hotelId, LocalDate date) {
        var out = date.plusDays(1);
        PriceSnapshot snap = snapshots
                .findByProviderAndExternalHotelIdAndCheckinDateAndCheckoutDate(provider, hotelId, date, out)
                .stream()
                .reduce((first, second) -> second) // get latest
                .orElse(null);

        var hotel = hotels.findByProviderAndExternalHotelId(provider, hotelId).orElse(null);

        return (snap == null)
                ? new PriceRow(hotelId,
                               hotel != null ? hotel.getNameCached() : null,
                               hotel != null ? hotel.getUrlCached() : null,
                               null, null, "NO_DATA")
                : new PriceRow(hotelId,
                               hotel != null ? hotel.getNameCached() : null,
                               hotel != null ? hotel.getUrlCached() : null,
                               snap.getCurrency(),
                               snap.getPriceTotal(),
                               snap.getAvailability());
    }

    public record MultiPriceSimple(String hotelId, String name, BigDecimal total) {}

  public MultiPriceSimple multiDaySimple(String provider, String hotelId, LocalDate checkin, LocalDate checkout) {
    var list = snapshots.findByProviderAndExternalHotelIdAndCheckinDateAndCheckoutDate(provider, hotelId, checkin, checkout);
    var s = list.isEmpty() ? null : list.get(list.size() - 1);
    var h = hotels.findByProviderAndExternalHotelId(provider, hotelId).orElse(null);
    String name = h != null ? h.getNameCached() : null;
    return (s == null) ? new MultiPriceSimple(hotelId, name, null)
                       : new MultiPriceSimple(hotelId, name, s.getPriceTotal());
  }

  public List<NightlySimpleRow> perNightSimple(
    String provider, String hotelId, LocalDate checkin, LocalDate checkout) {

  var hotel = hotels.findByProviderAndExternalHotelId(provider, hotelId).orElse(null);
  String name = hotel != null ? hotel.getNameCached() : null;

  var snaps = snapshots
      .findByProviderAndExternalHotelIdAndCheckinDateGreaterThanEqualAndCheckoutDateLessThanEqualOrderByCheckinDateAsc(
          provider, hotelId, checkin, checkout);

  return snaps.stream()
      .map(s -> new NightlySimpleRow(name, s.getCheckinDate(), s.getPriceTotal()))
      .toList();
}
    
}
