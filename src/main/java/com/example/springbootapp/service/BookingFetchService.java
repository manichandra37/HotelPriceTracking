package com.example.springbootapp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.example.springbootapp.entity.PriceSnapshot;
import com.example.springbootapp.repository.PriceSnapshotRepo;
import com.example.springbootapp.util.BookingApiClient;
import com.example.springbootapp.util.BookingMapper;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingFetchService {
    private final BookingApiClient api;
    private final PricingIngestService ingest;
    private final PriceSnapshotRepo snapshots;

    /** Fetch one stay (checkin→checkout) from RapidAPI and save */
    public void fetchAndSave(String hotelId, LocalDate checkin, LocalDate checkout) {
        JsonNode json = api.fetchStay(hotelId, checkin.toString(), checkout.toString());
        var n = BookingMapper.from(json);  // normalize JSON

        // upsert hotel + insert snapshot
        ingest.upsertHotel(n.provider(), n.externalHotelId(), n.name(), n.url(),
                n.city(), n.state(), n.country(), true);

        ingest.insertSnapshot(n.provider(), n.externalHotelId(), n.checkin(), n.checkout(),
                n.currency(), n.priceTotal(), n.pricePerNight(), n.availability());
    }

    /** Multi-night via nightly calls; stores nightly + aggregate snapshot */
  public void fetchAndSaveMultiNightSum(String hotelId, LocalDate checkin, LocalDate checkout) {
    final String PROVIDER = "RAPIDAPI_BOOKING";
    long nights = Duration.between(checkin.atStartOfDay(), checkout.atStartOfDay()).toDays();
    if (nights < 1 || nights > 30) throw new IllegalArgumentException("Nights must be 1..30");

    BigDecimal total = BigDecimal.ZERO;
    String aggAvail = "AVAILABLE";

    LocalDate d = checkin;
    while (d.isBefore(checkout)) {
      LocalDate dOut = d.plusDays(1);

      // cache window: 60 minutes
      var freshList = snapshots.findFresh(PROVIDER, hotelId, d, dOut, Instant.now().minus(Duration.ofMinutes(60)));
      PriceSnapshot snap = freshList.isEmpty() ? fetchNight(PROVIDER, hotelId, d, dOut) : freshList.get(0);

      total = total.add(snap.getPriceTotal());
      if ("SOLD_OUT".equals(snap.getAvailability())) { aggAvail = "SOLD_OUT"; break; }
      if ("LIMITED".equals(snap.getAvailability()))  { aggAvail = "LIMITED"; }

      d = dOut;
    }

    if (!"SOLD_OUT".equals(aggAvail)) {
      BigDecimal perNight = total.divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP);
      ingest.insertSnapshot(PROVIDER, hotelId, checkin, checkout, "USD", total, perNight, aggAvail);
    }
  }

  private PriceSnapshot fetchNight(String provider, String hotelId, LocalDate in, LocalDate out) {
    int attempts = 0;
    while (true) {
      attempts++;
      try {
        JsonNode json = api.fetchStay(hotelId, in.toString(), out.toString());
        var n = BookingMapper.from(json);
        ingest.upsertHotel(n.provider(), n.externalHotelId(), n.name(), n.url(), n.city(), n.state(), n.country(), true);
        return ingest.insertSnapshot(n.provider(), n.externalHotelId(), n.checkin(), n.checkout(),
            n.currency(), n.priceTotal(), n.pricePerNight(), n.availability());
      } catch (Exception e) {
        if (attempts >= 3) throw e;
        try { Thread.sleep(Math.min(2000L * attempts, 5000L)); } catch (InterruptedException ignored) {}
      }
    }
  }
}