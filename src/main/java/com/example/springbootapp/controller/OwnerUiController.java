package com.example.springbootapp.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.springbootapp.dto.NightlySimpleResponse;
import com.example.springbootapp.dto.NightlySimpleRow;
import com.example.springbootapp.dto.PriceTableResponse;
import com.example.springbootapp.dto.SingleDayTablePriceRow;
import com.example.springbootapp.dto.TableHotelRow;
import com.example.springbootapp.entity.ExternalHotel;
import com.example.springbootapp.entity.PriceTable;
import com.example.springbootapp.entity.PriceTableExternalHotel;
import com.example.springbootapp.repository.ExternalHotelRepo;
import com.example.springbootapp.repository.PriceSnapshotRepo;
import com.example.springbootapp.repository.PriceTableExternalHotelRepo;
import com.example.springbootapp.repository.PriceTableRepo;
import com.example.springbootapp.service.BookingFetchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ui")
@RequiredArgsConstructor
public class OwnerUiController {

    private final PriceTableRepo priceTables;
    private final PriceTableExternalHotelRepo tableLinks;
    private final ExternalHotelRepo externalHotels;
    private final PriceSnapshotRepo snapshots;
    private final BookingFetchService bookingFetchService;

    @Value("${app.fetch.throttle-ms:0}")
    private long configuredThrottleMs;

    // ==================== PRICE TABLE MANAGEMENT ====================

    /**
     * List all price tables for a given owner.
     * GET /api/ui/owner/{ownerId}/price-tables
     */
    @GetMapping("/owner/{ownerId}/price-tables")
    public List<PriceTableResponse> ownerTables(@PathVariable Long ownerId) {
        List<PriceTable> pts = priceTables.findByOwnerId(ownerId);
        return pts.stream()
                .map(pt -> new PriceTableResponse(
                        pt.getId(),
                        pt.getOwnerId(),
                        pt.getName(),
                        pt.getCityLabel()
                ))
                .toList();
    }

    /**
     * List hotels in a given price table (with owner flag).
     * GET /api/ui/price-tables/{priceTableId}/hotels
     */
    @GetMapping("/price-tables/{priceTableId}/hotels")
    public List<TableHotelRow> tableHotels(@PathVariable Long priceTableId) {
        // Ensure table exists (nice 404 if not)
        priceTables.findById(priceTableId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Price table not found"));

        // Fetch all links for this table
        List<PriceTableExternalHotel> links = tableLinks.findByIdPriceTableId(priceTableId);
        if (links.isEmpty()) {
            return List.of();
        }

        // Load ExternalHotel rows for those refs, map by internal id
        List<Long> hotelRefIds = links.stream()
                .map(l -> l.getId().getExternalHotelRef())
                .toList();

        Map<Long, ExternalHotel> hotelsById = externalHotels.findAllById(hotelRefIds).stream()
                .collect(Collectors.toMap(ExternalHotel::getId, h -> h));

        // Build UI rows
        return links.stream()
                .map(l -> {
                    ExternalHotel h = hotelsById.get(l.getId().getExternalHotelRef());
                    if (h == null) {
                        // Shouldn't happen; skip if mismatch
                        return null;
                    }
                    return new TableHotelRow(
                            h.getExternalHotelId(),
                            h.getNameCached(),
                            l.isOwner()
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    // ==================== PRICE DATA ENDPOINTS ====================

    /**
     * SINGLE DAY — prices for every hotel in a price table for a given date.
     * GET /api/ui/price-tables/{priceTableId}/single-day?date=YYYY-MM-DD&provider=RAPIDAPI_BOOKING
     */
    @GetMapping("/price-tables/{priceTableId}/single-day")
    public List<SingleDayTablePriceRow> tableSingleDay(
            @PathVariable Long priceTableId,
            @RequestParam String date,
            @RequestParam(defaultValue = "RAPIDAPI_BOOKING") String provider
    ) {
        var pt = priceTables.findById(priceTableId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Price table not found"));

        LocalDate d = LocalDate.parse(date);
        LocalDate out = d.plusDays(1);

        // Links for table
        List<PriceTableExternalHotel> links = tableLinks.findByIdPriceTableId(priceTableId);
        if (links.isEmpty()) {
            return List.of();
        }

        // Load hotels map
        List<Long> refIds = links.stream()
                .map(l -> l.getId().getExternalHotelRef())
                .toList();
        Map<Long, ExternalHotel> hotelsById = externalHotels.findAllById(refIds).stream()
                .collect(Collectors.toMap(ExternalHotel::getId, h -> h));

        List<SingleDayTablePriceRow> rows = new ArrayList<>();

        for (var link : links) {
            var h = hotelsById.get(link.getId().getExternalHotelRef());
            if (h == null) {
                continue;
            }

            var snapOpt = snapshots.findTopByProviderAndExternalHotelIdAndCheckinDateAndCheckoutDateOrderByFetchedAtDesc(
                    h.getProvider(), h.getExternalHotelId(), d, out);

            if (snapOpt.isPresent()) {
                var s = snapOpt.get();
                rows.add(new SingleDayTablePriceRow(
                        h.getExternalHotelId(),
                        Optional.ofNullable(h.getNameCached()).orElse("(unknown)"),
                        s.getCurrency(),
                        s.getPriceTotal(),
                        s.getAvailability(),
                        link.isOwner()
                ));
            } else {
                rows.add(new SingleDayTablePriceRow(
                        h.getExternalHotelId(),
                        Optional.ofNullable(h.getNameCached()).orElse("(unknown)"),
                        null,
                        null,
                        "NO_DATA",
                        link.isOwner()
                ));
            }
        }

        // Owner row first (optional)
        rows.sort((a, b) -> Boolean.compare(b.owner(), a.owner()));
        return rows;
    }

    /**
     * MULTI-DAY (nightly simple) — per hotel: return nightly rows for [checkin, checkout).
     * GET /api/ui/hotel/per-night-simple?provider=RAPIDAPI_BOOKING&externalHotelId=1046167&checkin=2025-08-27&checkout=2025-08-30
     */
    @GetMapping("/hotel/per-night-simple")
    public NightlySimpleResponse perNightSimple(
            @RequestParam String provider,
            @RequestParam String externalHotelId,
            @RequestParam String checkin,
            @RequestParam String checkout
    ) {
        LocalDate in = LocalDate.parse(checkin);
        LocalDate out = LocalDate.parse(checkout);

        // Load cached hotel name (optional)
        var hotelOpt = externalHotels.findByProviderAndExternalHotelId(provider, externalHotelId);
        String name = hotelOpt.map(ExternalHotel::getNameCached).orElse("(unknown)");

        var snaps = snapshots
                .findByProviderAndExternalHotelIdAndCheckinDateGreaterThanEqualAndCheckoutDateLessThanEqualOrderByCheckinDateAsc(
                        provider, externalHotelId, in, out);

        // Index by checkin date
        Map<LocalDate, com.example.springbootapp.entity.PriceSnapshot> byCheckin = snaps.stream()
                .collect(Collectors.toMap(
                        com.example.springbootapp.entity.PriceSnapshot::getCheckinDate,
                        s -> s,
                        (a, b) -> a // If duplicates, pick first
                ));

        List<NightlySimpleRow> rows = new ArrayList<>();
        for (LocalDate d = in; d.isBefore(out); d = d.plusDays(1)) {
            var s = byCheckin.get(d);
            rows.add(new NightlySimpleRow(
                    name,  // Hotel name
                    d,     // Date
                    (s != null) ? s.getPriceTotal() : null   // Already BigDecimal
            ));
        }

        return new NightlySimpleResponse(externalHotelId, name, rows);
    }

    // ==================== DATA FETCHING ENDPOINTS ====================

    /**
     * Fetch prices for all hotels in a table for a specific date.
     * GET /api/ui/owner/{ownerId}/price-tables/{tableId}/fetch?date=YYYY-MM-DD
     */
    @GetMapping("/owner/{ownerId}/price-tables/{tableId}/fetch")
    public String ownerFetchTableForDay(
            @PathVariable Long ownerId,
            @PathVariable Long tableId,
            @RequestParam String date
    ) {
        // 1) Check table belongs to this owner
        var table = priceTables.findById(tableId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Price table not found"));

        if (!Objects.equals(table.getOwnerId(), ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This table does not belong to you");
        }

        // 2) Parse date
        LocalDate d = LocalDate.parse(date);
        LocalDate out = d.plusDays(1);

        // 3) Load all hotel links for this table
        var links = tableLinks.findByIdPriceTableId(tableId);
        if (links.isEmpty()) {
            return "No hotels linked to table " + tableId;
        }

        // 4) Get hotel details
        var hotelIds = links.stream()
                .map(l -> l.getId().getExternalHotelRef())
                .toList();
        var hotels = externalHotels.findAllById(hotelIds);

        // 5) Trigger fetch for each hotel
        int count = 0;
        for (var h : hotels) {
            bookingFetchService.fetchSingle(h.getExternalHotelId(), d, out);
            count++;
        }

        return "Fetched prices for " + count + " hotels on " + d;
    }

    /**
     * Fetch prices for a specific hotel over a date range.
     * GET /api/ui/owner/{ownerId}/hotel/{externalHotelId}/fetch-range?from=YYYY-MM-DD&to=YYYY-MM-DD
     */
    @GetMapping("/owner/{ownerId}/hotel/{externalHotelId}/fetch-range")
    public String ownerFetchHotelRange(
            @PathVariable Long ownerId,
            @PathVariable String externalHotelId,
            @RequestParam String from,    // YYYY-MM-DD
            @RequestParam String to       // YYYY-MM-DD (exclusive end)
    ) {
        // Parse dates
        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);   // Fetch nights [start, end)

        // (Optional) quick guard: ensure this hotel is in at least one table of this owner
        // You can skip this if not needed right now.

        int nights = 0;
        for (LocalDate d = start; d.isBefore(end); d = d.plusDays(1)) {
            bookingFetchService.fetchSingle(externalHotelId, d, d.plusDays(1));
            nights++;
        }
        return "Fetched " + nights + " night(s) for hotel " + externalHotelId + " from " + start + " to " + end;
    }

    /**
     * Fetch prices for all hotels in a table over a date range.
     * GET /api/ui/owner/{ownerId}/price-tables/{tableId}/fetch-range?from=YYYY-MM-DD&to=YYYY-MM-DD
     */
    @GetMapping("/owner/{ownerId}/price-tables/{tableId}/fetch-range")
    public String ownerFetchTableRange(
            @PathVariable Long ownerId,
            @PathVariable Long tableId,
            @RequestParam String from,   // YYYY-MM-DD (inclusive)
            @RequestParam String to      // YYYY-MM-DD (exclusive)
    ) {
        // 1) Verify table belongs to owner
        var table = priceTables.findById(tableId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Price table not found"));
        if (!Objects.equals(table.getOwnerId(), ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This table does not belong to you");
        }

        // 2) Parse range
        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);   // Fetch nights [start, end)

        // 3) Load linked hotels
        var links = tableLinks.findByIdPriceTableId(tableId);
        if (links.isEmpty()) {
            return "No hotels linked to table " + tableId;
        }

        var hotelRefIds = links.stream()
                .map(l -> l.getId().getExternalHotelRef())
                .toList();
        var hotels = externalHotels.findAllById(hotelRefIds);

        // 4) Optional throttle between external calls (to avoid RapidAPI 429/403 on bursts)
        long throttleMs = fetchThrottleMs();

        int hotelsCount = 0;
        int nightsCount = 0;

        for (var h : hotels) {
            hotelsCount++;
            for (LocalDate d = start; d.isBefore(end); d = d.plusDays(1)) {
                bookingFetchService.fetchSingle(h.getExternalHotelId(), d, d.plusDays(1));
                nightsCount++;
                if (throttleMs > 0) {
                    try {
                        Thread.sleep(throttleMs);
                    } catch (InterruptedException ignored) {
                        // Ignore interruption
                    }
                }
            }
        }
        return "Fetched " + nightsCount + " night(s) across " + hotelsCount + " hotel(s) "
                + "for table " + tableId + " from " + start + " to " + end;
    }

    // ==================== HELPER METHODS ====================

    private long fetchThrottleMs() {
        return configuredThrottleMs;
    }
}
