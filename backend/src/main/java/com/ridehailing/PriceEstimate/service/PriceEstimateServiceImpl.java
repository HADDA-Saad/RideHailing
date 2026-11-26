package com.ridehailing.PriceEstimate.service;

import com.ridehailing.ride.service.RideService;

import com.ridehailing.auth.model.Role;

import com.ridehailing.PriceEstimate.model.PriceEstimate;
import com.ridehailing.PriceEstimate.repository.PriceEstimateRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PriceEstimateServiceImpl implements PriceEstimateService{
    private final PriceEstimateRepository repo;
    private final RideService rides;

    public PriceEstimateServiceImpl(PriceEstimateRepository repo, RideService rides) {
        this.repo = repo;
        this.rides = rides;
    }

    private static final BigDecimal BASE_FARE = BigDecimal.valueOf(5.0);
    private static final BigDecimal PER_KM_RATE = BigDecimal.valueOf(2.5);
    private static final BigDecimal SURGE_MULTIPLIER = BigDecimal.valueOf(1.0);
    // Both participants may view the estimate, but only the rider may request one for their ride.
    public PriceEstimate calculatePrice(Long rideId, BigDecimal distanceKm, Long userId){
        rides.findById(rideId, userId, Role.RIDER);
        BigDecimal price = BASE_FARE.add(distanceKm.multiply(PER_KM_RATE)).multiply(SURGE_MULTIPLIER) ;

        PriceEstimate estimate=new PriceEstimate();
        estimate.setRideId(rideId);
        estimate.setPrice(price);
        estimate.setDistance(distanceKm);

        return repo.save(estimate);
    }
    public PriceEstimate findByRideId(Long rideId, Long userId, Role role){
        rides.findById(rideId, userId, role);
        return repo.findByRideId(rideId).orElseThrow(()->new RuntimeException("Ride not found"));
    }
}
