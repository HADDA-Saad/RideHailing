package com.ridehailing.ride.service;

import com.ridehailing.driver.model.Driver;
import com.ridehailing.driver.repository.DriverRepository;
import com.ridehailing.ride.model.Ride;
import com.ridehailing.ride.model.RideStatus;
import com.ridehailing.ride.repository.RideRepository;
import com.ridehailing.rider.model.Rider;
import com.ridehailing.rider.repository.RiderRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
public class RideServiceImpl implements RideService{
    private final RideRepository rideRepo;
    private final DriverRepository driverRepo;

    public RideServiceImpl(
            RideRepository rideRepo,DriverRepository driverRepo,RiderRepository riderRepo){
        this.rideRepo=rideRepo;
        this.driverRepo = driverRepo;
    }
    public Ride requestRide(Long driverId,Long riderId){
        Ride ride=new Ride();
        ride.setRiderId(riderId);
        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ACTIVE);
        return rideRepo.save(ride);
    }


    public Ride findById(Long id){
        return rideRepo.findById(id).
                orElseThrow(()->new RuntimeException("Ride not found"));
    }

    public List<Ride> findByDriverId(Long id){
        return rideRepo.findByDriverId(id);
    }

    public List<Ride> findByRiderId(Long id){
        return rideRepo.findByRiderId(id);
    }

    @Override
    @Transactional
    public Ride completeRide(Long id, Long authenticatedUserId) {
        Driver driver = driverRepo.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new AccessDeniedException("Driver profile required"));
        Ride ride = rideRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
        if (!driver.getId().equals(ride.getDriverId())) {
            throw new AccessDeniedException("Ride is assigned to another driver");
        }
        ride.setStatus(RideStatus.COMPLETED);
        return rideRepo.save(ride);
    }
    public List<Ride> findByStatus(RideStatus status){
        return rideRepo.findByStatus(status);
    }
}
