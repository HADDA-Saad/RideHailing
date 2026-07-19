package com.ridehailing.location.repository;

import com.ridehailing.driver.model.Driver;
import com.ridehailing.location.model.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverLocationRepository extends JpaRepository<DriverLocation,Long> {
    List<DriverLocation> findByDriverId(Long id);
    Optional<DriverLocation> findTopByDriverIdOrderByTimestampDesc(Long DriverId);
}
