package com.example.springbootapp.dto;

import java.math.BigDecimal;

public record PriceRow(
    String hotelId,
    String name,
    String url,
    String currency,
    BigDecimal price,
    String availability
) {}