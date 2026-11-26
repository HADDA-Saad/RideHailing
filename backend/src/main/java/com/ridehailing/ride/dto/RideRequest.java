package com.ridehailing.ride.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RideRequest(@NotNull @Positive Long driverId) {
}
