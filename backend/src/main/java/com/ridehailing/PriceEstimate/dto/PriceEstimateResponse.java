package com.ridehailing.PriceEstimate.dto;

import com.ridehailing.PriceEstimate.model.PriceEstimate;

import java.math.BigDecimal;

public record PriceEstimateResponse(Long priceId, Long rideId, BigDecimal price, BigDecimal distance,BigDecimal BaseFare,BigDecimal perKmRate, BigDecimal surgeMultiplier) {
    public static PriceEstimateResponse from(PriceEstimate estimate) {
        return new PriceEstimateResponse(
                estimate.getPriceId(),
                estimate.getRideId(),
                estimate.getPrice(),
                estimate.getDistance(),
                estimate.getBaseFare(),
                estimate.getPerKmRate(),
                estimate.getSurgeMultiplier()
        );
    }
}

