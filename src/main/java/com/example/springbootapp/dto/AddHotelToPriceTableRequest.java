package com.example.springbootapp.dto;

public record AddHotelToPriceTableRequest(
    String externalHotelId,   // hotel_id from API
    boolean isOwnerHotel,     // true if it’s the owner’s hotel, false if competitor
    String provider,          // e.g. "RAPIDAPI_BOOKING"
    String name,              // cached hotel name
    String url,               // cached hotel URL
    String city,
    String country
) {}