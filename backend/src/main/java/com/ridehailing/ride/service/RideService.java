package com.ridehailing.ride.service;

import com.ridehailing.ride.model.Ride;
import com.ridehailing.ride.model.RideStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface RideService {
    @PreAuthorize("hasRole('RIDER')")
    Ride requestRide(Long driverId, Long authenticatedUserId);
    Ride findById(Long id);
    List<Ride> findByDriverId(Long DriverId);
    List<Ride> findByRiderId(Long id);
    @PreAuthorize("hasRole('DRIVER')")
    Ride completeRide(Long id, Long authenticatedUserId);
    List<Ride> findByStatus(RideStatus status);
}
