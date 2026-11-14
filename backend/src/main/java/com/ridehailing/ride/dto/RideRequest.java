package com.ridehailing.ride.dto;

import com.ridehailing.ride.model.RideStatus;

public record RideRequest(Long driverId, Long riderId) {
}
