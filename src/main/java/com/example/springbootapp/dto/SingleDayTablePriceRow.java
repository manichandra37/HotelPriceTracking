package com.example.springbootapp.dto;

import java.math.BigDecimal;

public record SingleDayTablePriceRow(
    String externalHotelId,
    String name,
    String currency,
    BigDecimal price,   // <- must be BigDecimal here
    String availability,
    boolean owner
) {}