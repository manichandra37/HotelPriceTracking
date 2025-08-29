package com.example.springbootapp.dto;

import java.util.List;

public record NightlySimpleResponse(
        String externalHotelId,
        String name,
        List<NightlySimpleRow> nights
) {}