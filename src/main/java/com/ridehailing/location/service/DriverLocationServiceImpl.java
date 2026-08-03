package com.ridehailing.location.service;

import com.ridehailing.driver.repository.DriverRepository;
import com.ridehailing.location.model.DriverLocation;
import com.ridehailing.location.repository.DriverLocationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DriverLocationServiceImpl implements DriverLocationService{
    private final DriverLocationRepository repo;
    private final DriverRepository driverRepo;
    public DriverLocationServiceImpl(DriverLocationRepository repo ,DriverRepository driverRepo){
        this.repo=repo;
        this.driverRepo=driverRepo;
    }
    public DriverLocation saveLocation(Long driverId, double lat, double lng){
        DriverLocation location=new DriverLocation();
        location.setDriverId(driverId);
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setTimestamp(LocalDate.now());
        return repo.save(location);
    }

    public DriverLocation getLatest(Long driverId){
        return repo.findTopByDriverIdOrderByTimestampDesc(driverId).orElseThrow(()->new RuntimeException("Location not found"));
    }
     public List<DriverLocation> getHistory(Long driverId){
        return repo.findByDriverId(driverId);
     }
}
