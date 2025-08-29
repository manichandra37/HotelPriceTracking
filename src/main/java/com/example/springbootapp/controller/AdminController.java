package com.example.springbootapp.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.springbootapp.dto.AddHotelToPriceTableRequest;
import com.example.springbootapp.dto.AdminActionResponse;
import com.example.springbootapp.dto.CreatePriceTableRequest;
import com.example.springbootapp.dto.OwnerResponse;
import com.example.springbootapp.dto.PriceTableResponse;
import com.example.springbootapp.dto.UserResponse;
import com.example.springbootapp.entity.ExternalHotel;
import com.example.springbootapp.entity.OwnerAccount;
import com.example.springbootapp.entity.PriceTable;
import com.example.springbootapp.entity.PriceTableExternalHotel;
import com.example.springbootapp.entity.UserStatus;
import com.example.springbootapp.repository.ExternalHotelRepo;
import com.example.springbootapp.repository.OwnerAccountRepo;
import com.example.springbootapp.repository.PriceTableExternalHotelRepo;
import com.example.springbootapp.repository.PriceTableRepo;
import com.example.springbootapp.repository.UserRepository;
import com.example.springbootapp.util.UserMapper;

import lombok.RequiredArgsConstructor;

/**
 * Admin controller for managing users, owners, and price tables.
 * Requires X-ADMIN-KEY header for all operations.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository users;
    private final OwnerAccountRepo ownerAccounts;
    private final PriceTableRepo priceTables;
    private final ExternalHotelRepo externalHotels;
    private final PriceTableExternalHotelRepo priceTableExternalHotels;

    // ==================== USER MANAGEMENT ====================

    /**
     * Get all pending users awaiting approval.
     * GET /api/admin/users/pending
     */
    @GetMapping("/users/pending")
    public List<UserResponse> pending(@RequestHeader("X-ADMIN-KEY") String key) {
        requireAdmin(key);
        return users.findByStatus(UserStatus.PENDING)
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    /**
     * Approve a pending user and create their owner account.
     * POST /api/admin/users/{id}/approve
     */
    @PostMapping("/users/{id}/approve")
    public AdminActionResponse approve(
            @RequestHeader("X-ADMIN-KEY") String key,
            @PathVariable Long id) {

        requireAdmin(key);

        var u = users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        u.setStatus(UserStatus.APPROVED);
        users.save(u);

        // Create OwnerAccount when user approved
        OwnerAccount account = OwnerAccount.builder()
                .userId(u.getId())
                .companyName(u.getName() + " Hotels") // Default, can be edited later
                .active(true)
                .build();
        ownerAccounts.save(account);

        return new AdminActionResponse(
                u.getId(),
                account.getId(),          // Return ownerId
                u.getName(),
                u.getEmail(),
                u.getStatus().name(),
                "User approved and owner account created"
        );
    }

    // ==================== OWNER MANAGEMENT ====================

    /**
     * Get all owner accounts.
     * GET /api/admin/owners
     */
    @GetMapping("/owners")
    public List<OwnerResponse> owners(@RequestHeader("X-ADMIN-KEY") String key) {
        requireAdmin(key);
        return ownerAccounts.findAll().stream()
                .map(o -> new OwnerResponse(o.getId(), o.getUserId(), o.getCompanyName(), o.isActive()))
                .toList();
    }

    // ==================== PRICE TABLE MANAGEMENT ====================

    /**
     * Create a new price table for an owner.
     * POST /api/admin/owners/{ownerId}/price-tables
     */
    @PostMapping("/owners/{ownerId}/price-tables")
    public PriceTableResponse createPriceTable(
            @RequestHeader("X-ADMIN-KEY") String key,
            @PathVariable Long ownerId,
            @RequestBody CreatePriceTableRequest req) {

        requireAdmin(key);

        PriceTable pt = PriceTable.builder()
                .ownerId(ownerId)
                .name(req.name())
                .cityLabel(req.cityLabel())
                .build();

        pt = priceTables.save(pt);

        return new PriceTableResponse(pt.getId(), pt.getOwnerId(), pt.getName(), pt.getCityLabel());
    }

    /**
     * Add a hotel to a price table.
     * POST /api/admin/price-tables/{priceTableId}/hotels
     */
    @PostMapping("/price-tables/{priceTableId}/hotels")
    public String addHotelToPriceTable(
            @RequestHeader("X-ADMIN-KEY") String key,
            @PathVariable Long priceTableId,
            @RequestBody AddHotelToPriceTableRequest req) {

        requireAdmin(key);

        // 1. Ensure external hotel exists or create it
        ExternalHotel hotel = externalHotels
                .findByProviderAndExternalHotelId(req.provider(), req.externalHotelId())
                .orElseGet(() -> externalHotels.save(
                        ExternalHotel.builder()
                                .provider(req.provider())
                                .externalHotelId(req.externalHotelId())
                                .nameCached(req.name())
                                .urlCached(req.url())
                                .cityCached(req.city())
                                .countryCached(req.country())
                                .isActive(true)
                                .lastSeenAt(Instant.now())
                                .build()
                ));

        // 2. Build composite key for link
        PriceTableExternalHotel.Id linkId =
                new PriceTableExternalHotel.Id(priceTableId, hotel.getId());

        // 3. Create link entity
        PriceTableExternalHotel link =
                new PriceTableExternalHotel(linkId, req.isOwnerHotel());

        // 4. Save link
        priceTableExternalHotels.save(link);

        return "Hotel " + req.externalHotelId() + " linked to price table " + priceTableId;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Verify admin authentication using X-ADMIN-KEY header.
     */
    private void requireAdmin(String key) {
        String expected = System.getenv().getOrDefault("ADMIN_KEY", "dev-admin-key");
        if (!expected.equals(key)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not admin");
        }
    }
}
