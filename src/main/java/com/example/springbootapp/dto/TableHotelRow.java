package com.example.springbootapp.dto;

public record TableHotelRow(
    String externalHotelId,  // e.g. "1046167"
    String name,             // cached name
    boolean owner            // true if the owner’s hotel for this table
) {}
