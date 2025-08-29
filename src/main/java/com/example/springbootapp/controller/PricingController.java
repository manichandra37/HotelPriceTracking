package com.example.springbootapp.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbootapp.dto.NightlySimpleRow;
import com.example.springbootapp.dto.PriceRow;
import com.example.springbootapp.dto.SingleDayListRow;
import com.example.springbootapp.entity.PriceSnapshot;
import com.example.springbootapp.repository.PriceSnapshotRepo;
import com.example.springbootapp.service.BookingFetchService;
import com.example.springbootapp.service.PriceTableUiService;
import com.example.springbootapp.service.PricingIngestService;
import com.example.springbootapp.service.UiPriceService;
import com.example.springbootapp.service.UiPriceService.MultiPriceSimple;
import com.example.springbootapp.util.BookingMapper;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for pricing operations including data ingestion, fetching, and UI endpoints.
 * Handles both single-day and multi-day pricing operations.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PricingController {

    private final PricingIngestService ingest;
    private final PriceSnapshotRepo snapshots;
    private final BookingFetchService bookingFetchService;
    private final UiPriceService uiPriceService;
    private final PriceTableUiService priceTableUiService;

    // ==================== DATA INGESTION ====================

    /**
     * Ingest one Booking/RapidAPI JSON for a single stay (single-day or multi-day).
     * POST /api/ingest/booking
     */
    @PostMapping("/ingest/booking")
    public String ingestBooking(@RequestBody JsonNode payload) {
        var n = BookingMapper.from(payload);
        ingest.upsertHotel(n.provider(), n.externalHotelId(), n.name(), n.url(), n.city(), n.state(), n.country(), true);
        ingest.insertSnapshot(
                n.provider(), n.externalHotelId(), n.checkin(), n.checkout(),
                n.currency(), n.priceTotal(), n.pricePerNight(), n.availability()
        );
        return "ok";
    }

    // ==================== DATA RETRIEVAL ====================

    /**
     * Read a single-day price (date -> date+1) for one hotel.
     * GET /api/prices/single-day
     */
    @GetMapping("/prices/single-day")
    public List<PriceSnapshot> singleDay(
            @RequestParam String provider,
            @RequestParam String externalHotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return snapshots.findByProviderAndExternalHotelIdAndCheckinDateAndCheckoutDate(
                provider, externalHotelId, date, date.plusDays(1));
    }

    /**
     * Read a multi-day price for one hotel.
     * GET /api/prices/multi-day
     */
    @GetMapping("/prices/multi-day")
    public List<PriceSnapshot> multiDay(
            @RequestParam String provider,
            @RequestParam String externalHotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout
    ) {
        return snapshots.findAll().stream()
                .filter(s -> s.getProvider().equals(provider)
                        && s.getExternalHotelId().equals(externalHotelId)
                        && s.getCheckinDate().equals(checkin)
                        && s.getCheckoutDate().equals(checkout))
                .toList();
    }

    // ==================== DATA FETCHING ====================

    /**
     * Fetch and save pricing data for a single hotel stay.
     * POST /api/fetch/single
     */
    @PostMapping("/fetch/single")
    public String fetchSingle(
            @RequestParam String hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout) {
        bookingFetchService.fetchAndSave(hotelId, checkin, checkout);
        return "ok";
    }

    /**
     * Trigger nightly-sum aggregation and save aggregate snapshot.
     * POST /api/fetch/multi-sum
     */
    @PostMapping("/fetch/multi-sum")
    public String fetchMultiSum(
            @RequestParam String hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout) {
        bookingFetchService.fetchAndSaveMultiNightSum(hotelId, checkin, checkout);
        return "ok";
    }

    // ==================== UI ENDPOINTS ====================

    /**
     * Get single-day pricing data for UI display.
     * GET /api/ui/single-day
     */
    @GetMapping("/ui/single-day")
    public PriceRow uiSingleDay(
            @RequestParam String provider,
            @RequestParam String externalHotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return uiPriceService.singleDay(provider, externalHotelId, date);
    }

    /**
     * Get multi-day simple pricing data for UI display (name + total).
     * GET /api/ui/multi-day-simple
     */
    @GetMapping("/ui/multi-day-simple")
    public MultiPriceSimple uiMultiSimple(
            @RequestParam String provider,
            @RequestParam String externalHotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout) {
        return uiPriceService.multiDaySimple(provider, externalHotelId, checkin, checkout);
    }

    /**
     * Get per-night simple pricing data for UI display.
     * GET /api/ui/per-night-simple
     */
    @GetMapping("/ui/per-night-simple")
    public List<NightlySimpleRow> uiPerNightSimple(
            @RequestParam String provider,
            @RequestParam String externalHotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout) {
        return uiPriceService.perNightSimple(provider, externalHotelId, checkin, checkout);
    }

    /**
     * Get single-day pricing data for a price table.
     * GET /api/ui/price-table/single-day
     */
    @GetMapping("/ui/price-table/single-day")
    public List<SingleDayListRow> uiPriceTableSingleDay(
            @RequestParam Long priceTableId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return priceTableUiService.singleDayList(priceTableId, date);
    }
}