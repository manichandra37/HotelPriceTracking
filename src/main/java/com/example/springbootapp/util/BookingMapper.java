package com.example.springbootapp.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BookingMapper {
    
    public record NormalizedHotelPrice(
      String provider, String externalHotelId,
      String name, String url, String city, String state, String country,
      String currency, BigDecimal priceTotal, BigDecimal pricePerNight,
      String availability, LocalDate checkin, LocalDate checkout
  ) {}

  public static NormalizedHotelPrice from(JsonNode root) {
    JsonNode d = root.path("data");
    String provider = "RAPIDAPI_BOOKING";
    String extId = d.path("hotel_id").asText();
    String name = d.path("hotel_name").asText(null);
    String url  = d.path("url").asText(null);
    String city = d.path("city_trans").asText(d.path("city").asText(null));
    String state = parseState(d.path("zip").asText(null));
    String country = d.path("countrycode").asText("US").toUpperCase();

    JsonNode gross = d.path("product_price_breakdown").path("gross_amount_hotel_currency");
    String currency = gross.path("currency").asText(d.path("currency_code").asText("USD"));
    BigDecimal total = gross.has("value") ? gross.path("value").decimalValue() : BigDecimal.ZERO;

    String arrivalDateStr = d.path("arrival_date").asText();
    String departureDateStr = d.path("departure_date").asText();
    
    if (arrivalDateStr == null || arrivalDateStr.trim().isEmpty() || 
        departureDateStr == null || departureDateStr.trim().isEmpty()) {
      throw new IllegalArgumentException("Arrival date or departure date is missing or empty");
    }
    
    LocalDate in  = LocalDate.parse(arrivalDateStr);
    LocalDate out = LocalDate.parse(departureDateStr);
    long nights = ChronoUnit.DAYS.between(in, out);
    BigDecimal perNight = (nights > 0) ? total.divide(BigDecimal.valueOf(nights), 2, java.math.RoundingMode.HALF_UP) : total;

    String availability = mapAvail(d.path("soldout").asInt(0), d.path("available_rooms").asInt(0), d.path("is_closed").asInt(0));
    return new NormalizedHotelPrice(provider, extId, name, url, city, state, country, currency, total, perNight, availability, in, out);
  }

  private static String parseState(String zipLike) {
    if (zipLike == null) return null;
    String[] p = zipLike.trim().split("\\s+");
    return p.length > 0 ? p[0].toUpperCase() : null;
  }
  private static String mapAvail(int soldout, int rooms, int closed) {
    if (soldout == 1 || closed == 1 || rooms <= 0) return "SOLD_OUT";
    if (rooms <= 3) return "LIMITED";
    return "AVAILABLE";
  }
}
