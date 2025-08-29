package com.example.springbootapp.dto;

import java.math.BigDecimal;

public record BookingApiResponse(Data data) {
    public record Data(
        String hotel_name,
        int available_rooms,
        ProductPriceBreakdown product_price_breakdown
    ) {}

    public record ProductPriceBreakdown(
        GrossAmount gross_amount
    ) {}

    public record GrossAmount(
        String currency,
        BigDecimal value
    ) {}
}