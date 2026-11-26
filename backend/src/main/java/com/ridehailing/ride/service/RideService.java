package com.ridehailing.ride.service;

import com.ridehailing.auth.model.Role;

import com.ridehailing.ride.model.Ride;
import com.ridehailing.ride.model.RideStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface RideService {
    @PreAuthorize("hasRole('RIDER')")
    Ride requestRide(Long driverId, Long authenticatedUserId);
    @PreAuthorize("hasAnyRole('RIDER', 'DRIVER')")
    Ride findById(Long id, Long userId, Role role);
    @PreAuthorize("hasRole('DRIVER')")
    List<Ride> findByDriverId(Long id, Long userId);
    @PreAuthorize("hasRole('RIDER')")
    List<Ride> findByRiderId(Long id, Long userId);
    @PreAuthorize("hasRole('DRIVER')")
    Ride completeRide(Long id, Long authenticatedUserId);
    @PreAuthorize("hasAnyRole('RIDER', 'DRIVER')")
    List<Ride> findByStatus(RideStatus status, Long userId, Role role);
}
