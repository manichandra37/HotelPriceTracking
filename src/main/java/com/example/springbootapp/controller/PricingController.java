package com.example.springbootapp.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.springbootapp.dto.AdminActionResponse;
import com.example.springbootapp.dto.NightlySimpleRow;
import com.example.springbootapp.dto.PriceRow;
import com.example.springbootapp.dto.SignupRequest;
import com.example.springbootapp.dto.SingleDayListRow;
import com.example.springbootapp.dto.UserResponse;
import com.example.springbootapp.entity.PriceSnapshot;
import com.example.springbootapp.entity.User;
import com.example.springbootapp.entity.UserStatus;
import com.example.springbootapp.repository.PriceSnapshotRepo;
import com.example.springbootapp.repository.UserRepository;
import com.example.springbootapp.service.BookingFetchService;
import com.example.springbootapp.service.PriceTableUiService;
import com.example.springbootapp.service.PricingIngestService;
import com.example.springbootapp.service.UiPriceService;
import com.example.springbootapp.service.UiPriceService.MultiPriceSimple;
import com.example.springbootapp.util.BookingApiClient;
import com.example.springbootapp.util.BookingMapper;
import com.example.springbootapp.util.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PricingController {

  private final PricingIngestService ingest;
  private final PriceSnapshotRepo snapshots;
  private final ObjectMapper om;
  private final BookingApiClient bookingApiClient;
  private final BookingFetchService bookingFetchService;
  private final UiPriceService uiPriceService;
  private final PriceTableUiService priceTableUiService;
  private final UserRepository users;

  // Admin key validation method
  private void requireAdmin(String key) {
    if (!"admin-secret-key".equals(key)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin key");
    }
  }

  // A) Ingest one Booking/RapidAPI JSON for a single stay (single-day or multi-day)
  @PostMapping("/ingest/booking")
  public String ingestBooking(@RequestBody JsonNode payload) {
    var n = BookingMapper.from(payload); // see 4.2 below
    ingest.upsertHotel(n.provider(), n.externalHotelId(), n.name(), n.url(), n.city(), n.state(), n.country(), true);
    ingest.insertSnapshot(
        n.provider(), n.externalHotelId(), n.checkin(), n.checkout(),
        n.currency(), n.priceTotal(), n.pricePerNight(), n.availability()
    );
    return "ok";
  }

  // B) Read a single-day price (date -> date+1) for one hotel
  @GetMapping("/prices/single-day")
public List<PriceSnapshot> singleDay(
    @RequestParam String provider,
    @RequestParam String externalHotelId,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
) {
  return snapshots.findByProviderAndExternalHotelIdAndCheckinDateAndCheckoutDate(
      provider, externalHotelId, date, date.plusDays(1));
}

  // C) Read a multi-day price for one hotel
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

  @PostMapping("/fetch/single")
public String fetchSingle(
    @RequestParam String hotelId,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout) {
  bookingFetchService.fetchAndSave(hotelId, checkin, checkout);
  return "ok";
}

@GetMapping("/ui/single-day")
public PriceRow uiSingleDay(
        @RequestParam String provider,
        @RequestParam String externalHotelId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return uiPriceService.singleDay(provider, externalHotelId, date);
}

// Trigger nightly-sum aggregation and save aggregate snapshot
@PostMapping("/fetch/multi-sum")
public String fetchMultiSum(
    @RequestParam String hotelId,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout) {
  bookingFetchService.fetchAndSaveMultiNightSum(hotelId, checkin, checkout);
  return "ok";
}

// UI: just name + total for the multi-night stay
@GetMapping("/ui/multi-day-simple")
public MultiPriceSimple uiMultiSimple(
    @RequestParam String provider,
    @RequestParam String externalHotelId,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout) {
  return uiPriceService.multiDaySimple(provider, externalHotelId, checkin, checkout);
}    

// PricingController.java (add)
@GetMapping("/ui/per-night-simple")
public List<NightlySimpleRow> uiPerNightSimple(
    @RequestParam String provider,
    @RequestParam String externalHotelId,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout) {
  return uiPriceService.perNightSimple(provider, externalHotelId, checkin, checkout);
}

@GetMapping("/ui/price-table/single-day")
public List<SingleDayListRow> uiPriceTableSingleDay(
    @RequestParam Long priceTableId,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
  return priceTableUiService.singleDayList(priceTableId, date);
}

@PostMapping("/auth/signup")
public UserResponse signup(@RequestBody @Valid SignupRequest req) {
    if (users.existsByEmail(req.email())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email exists");
    }
    String hash = BCrypt.withDefaults().hashToString(12, req.password().toCharArray());
    User u = User.builder()
            .name(req.name())
            .email(req.email())
            .phone(req.phone())
            .passwordHash(hash)
            .status(UserStatus.PENDING)
            .build();
    users.save(u);
    return UserMapper.toResponse(u);
}

@GetMapping("/admin/users/pending")
public List<UserResponse> pending(@RequestHeader("X-ADMIN-KEY") String key) {
    requireAdmin(key);
    return users.findByStatus(UserStatus.PENDING)
                .stream()
                .map(UserMapper::toResponse)
                .toList();
}

@PostMapping("/admin/users/{id}/approve")
public AdminActionResponse approve(@RequestHeader("X-ADMIN-KEY") String key, @PathVariable Long id) {
    requireAdmin(key);
    User u = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    u.setStatus(UserStatus.APPROVED);
    users.save(u);
    return new AdminActionResponse(u.getId(), u.getName(), u.getEmail(), u.getStatus().name(), "User approved successfully");
}


}