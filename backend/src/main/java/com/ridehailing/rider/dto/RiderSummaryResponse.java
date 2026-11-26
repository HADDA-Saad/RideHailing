package com.ridehailing.rider.dto;

import com.ridehailing.rider.model.Rider;

public record RiderSummaryResponse(Long id, String name) {
    public static RiderSummaryResponse from(Rider profile) {
        return new RiderSummaryResponse(profile.getId(), profile.getName());
    }
}
