package com.ridehailing.driver.dto;

import com.ridehailing.driver.model.Driver;

public record DriverSummaryResponse(Long id, String name, boolean available) {
    public static DriverSummaryResponse from(Driver profile) {
        return new DriverSummaryResponse(profile.getId(), profile.getName(), profile.isAvailable());
    }
}
