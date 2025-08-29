package com.example.springbootapp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NightlySimpleRow(
    String hotelName,
    LocalDate date,
    BigDecimal price
) {}