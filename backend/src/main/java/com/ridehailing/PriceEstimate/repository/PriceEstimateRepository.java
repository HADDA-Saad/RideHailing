package com.ridehailing.PriceEstimate.repository;

import com.ridehailing.PriceEstimate.model.PriceEstimate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriceEstimateRepository extends JpaRepository<PriceEstimate,Long> {

    Optional<PriceEstimate> findByRideId(Long rideId);
}