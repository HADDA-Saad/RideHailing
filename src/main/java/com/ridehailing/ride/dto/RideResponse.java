package com.ridehailing.ride.dto;

import com.ridehailing.ride.model.RideStatus;
import com.ridehailing.ride.model.Ride;

public record RideResponse(Long id, Long driverId, Long riderId, double price, RideStatus status) {
    public static RideResponse from(Ride ride){
        return new RideResponse(
                ride.getId(),ride.getDriver().getId(),ride.getRider().getId(),ride.getPrice(),ride.getStatus());
    }


}
