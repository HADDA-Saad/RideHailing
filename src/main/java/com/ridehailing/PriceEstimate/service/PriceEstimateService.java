package com.ridehailing.PriceEstimate.service;

import com.ridehailing.PriceEstimate.model.PriceEstimate;

import java.math.BigDecimal;

public interface PriceEstimateService {
    PriceEstimate calculatePrice(Long rideId, BigDecimal distanceKm);
    PriceEstimate findByRideId(Long rideId);

}
