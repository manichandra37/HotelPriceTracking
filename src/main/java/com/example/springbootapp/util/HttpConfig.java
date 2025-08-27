package com.example.springbootapp.util;

import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpConfig {
  @Bean WebClient bookingClient() {
    return WebClient.builder().build();
  }
}
