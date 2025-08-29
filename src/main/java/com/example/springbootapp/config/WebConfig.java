package com.example.springbootapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration class for CORS and WebClient setup.
 * Configures cross-origin resource sharing and external API client.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ==================== CORS CONFIGURATION ====================

    /**
     * Configure CORS mappings for API endpoints.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    // ==================== WEB CLIENT CONFIGURATION ====================

    /**
     * Create WebClient bean for external booking API calls.
     * 
     * @param baseUrl Base URL for the booking API
     * @param host Host header for RapidAPI
     * @param key API key for authentication
     * @return Configured WebClient instance
     */
    @Bean
    public WebClient bookingWebClient(
            @Value("${booking.api.base}") String baseUrl,
            @Value("${booking.api.host}") String host,
            @Value("${booking.api.key}") String key
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl)                            // https://booking-com15.p.rapidapi.com
                .defaultHeader("X-RapidAPI-Host", host)      // booking-com15.p.rapidapi.com
                .defaultHeader("X-RapidAPI-Key", key)        // Your real key
                .build();
    }
}
