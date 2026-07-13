package com.ridehailing.ride.service;

import com.ridehailing.ride.model.Ride;

public interface RideService {
    Ride RequestRide(Long driverId,Long riderId);
    Ride findById(Long id);
    Ride completeRide(Long id);
}
