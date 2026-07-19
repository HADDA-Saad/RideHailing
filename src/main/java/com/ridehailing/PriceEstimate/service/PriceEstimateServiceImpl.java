package com.ridehailing.PriceEstimate.service;

import com.ridehailing.PriceEstimate.model.PriceEstimate;
import com.ridehailing.PriceEstimate.repository.PriceEstimateRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PriceEstimateServiceImpl implements PriceEstimateService{
    private final PriceEstimateRepository repo;
    public PriceEstimateServiceImpl(PriceEstimateRepository repo){
        this.repo=repo;
    }

    private static final BigDecimal BASE_FARE = BigDecimal.valueOf(5.0);
    private static final BigDecimal PER_KM_RATE = BigDecimal.valueOf(2.5);
    private static final BigDecimal SURGE_MULTIPLIER = BigDecimal.valueOf(1.0);
    public PriceEstimate calculatePrice(Long rideId, BigDecimal distanceKm){
        BigDecimal price = BASE_FARE.add(distanceKm.multiply(PER_KM_RATE)).multiply(SURGE_MULTIPLIER) ;

        PriceEstimate estimate=new PriceEstimate();
        estimate.setRideId(rideId);
        estimate.setPrice(price);
        estimate.setDistance(distanceKm);

        return repo.save(estimate);
    }
    public PriceEstimate findByRideId(Long rideId){
        return repo.findByRideId(rideId).orElseThrow(()->new RuntimeException("Ride not found"));
    }
}
