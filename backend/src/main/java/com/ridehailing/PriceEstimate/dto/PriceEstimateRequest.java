package com.ridehailing.PriceEstimate.dto;

import java.math.BigDecimal;

    public record PriceEstimateRequest (Long rideId, BigDecimal distanceKm){
}
