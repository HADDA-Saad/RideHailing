package com.ridehailing.PriceEstimate.service;

import org.springframework.security.access.prepost.PreAuthorize;

import com.ridehailing.auth.model.Role;

import com.ridehailing.PriceEstimate.model.PriceEstimate;

import java.math.BigDecimal;

public interface PriceEstimateService {
    @PreAuthorize("hasRole('RIDER')")
    PriceEstimate calculatePrice(Long rideId, BigDecimal distanceKm, Long userId);
    @PreAuthorize("hasAnyRole('RIDER', 'DRIVER')")
    PriceEstimate findByRideId(Long rideId, Long userId, Role role);

}
