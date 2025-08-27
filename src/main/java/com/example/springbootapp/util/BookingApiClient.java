package com.example.springbootapp.util;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

// BookingApiClient.java
@Component
@RequiredArgsConstructor
public class BookingApiClient {
  private final WebClient client = WebClient.builder().build();

  @Value("${booking.api.base}") String base;
  @Value("${booking.api.host}") String host;
  @Value("${booking.api.key}")  String key;

  public JsonNode fetchStay(String hotelId, String checkin, String checkout) {
    System.out.println("DEBUG: base=" + base + ", host=" + host + ", key=" + key);
    String url = base + "/api/v1/hotels/getHotelDetails?hotel_id=" + hotelId + 
           "&arrival_date=" + checkin + 
           "&departure_date=" + checkout + 
           "&adults=1&children_age=1,17&room_qty=1&units=metric&temperature_unit=c&languagecode=en-us&currency_code=USD";
    System.out.println("DEBUG: URL=" + url);
    return client.get()
      .uri(url)
      .header("X-RapidAPI-Host", host)
      .header("X-RapidAPI-Key",  key)
      .retrieve()
      .bodyToMono(JsonNode.class)
      .block();
  }
}
