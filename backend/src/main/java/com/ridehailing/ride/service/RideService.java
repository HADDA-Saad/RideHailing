package com.ridehailing.ride.service;

import com.ridehailing.ride.model.Ride;
import com.ridehailing.ride.model.RideStatus;

import java.util.List;

public interface RideService {
    Ride requestRide(Long driverId,Long riderId);
    Ride findById(Long id);
    List<Ride> findByDriverId(Long DriverId);
    List<Ride> findByRiderId(Long id);
    Ride completeRide(Long id);
    List<Ride> findByStatus(RideStatus status);
}
