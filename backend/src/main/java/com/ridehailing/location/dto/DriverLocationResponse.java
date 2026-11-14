package com.ridehailing.location.dto;

import com.ridehailing.location.model.DriverLocation;

import java.time.LocalDateTime;

public record DriverLocationResponse(Long id,Long driverId, double latitude, double longitude, LocalDateTime timestamp) {
    public static DriverLocationResponse from(DriverLocation location){
        return new DriverLocationResponse(
                location.getId(),
                location.getDriverId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getTimestamp()
        );
    }

}
