package com.ridehailing.location.service;

import com.ridehailing.location.model.DriverLocation;
import com.ridehailing.location.repository.DriverLocationRepository;

import java.time.LocalDate;
import java.util.List;

public class DriverLocationServiceImpl implements DriverLocationService{
    private DriverLocationRepository repo;
    public DriverLocationServiceImpl(DriverLocationRepository repo){this.repo=repo;}
    public DriverLocation saveLocation(Long driverId, double lat, double lng){
        DriverLocation location=new DriverLocation();
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setTimestamp(LocalDate.now());
        return location;
    }

    public DriverLocation getLatest(Long driverId){
        return repo.findTopByDriverIdOrderByTimestampDesc(driverId).orElseThrow(()->new RuntimeException("Location not found"));
    }
     public List<DriverLocation> getHistory(Long driverId){
        return repo.findByDriverId(driverId);
     }
}
