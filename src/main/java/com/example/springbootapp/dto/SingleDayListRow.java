package com.example.springbootapp.dto;

import java.math.BigDecimal;

public record SingleDayListRow(
  Long priceTableId,
  String hotelId,
  String name,
  BigDecimal price,
  boolean owner    // bold in UI
) {}