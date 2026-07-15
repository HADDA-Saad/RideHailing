package com.ridehailing.ride.service;

import com.ridehailing.driver.model.Driver;
import com.ridehailing.driver.repository.DriverRepository;
import com.ridehailing.ride.model.Ride;
import com.ridehailing.ride.model.RideStatus;
import com.ridehailing.ride.repository.RideRepository;
import com.ridehailing.rider.model.Rider;
import com.ridehailing.rider.repository.RiderRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RideServiceImpl implements RideService{
    private final RideRepository rideRepo;
    private final DriverRepository driverRepo;
    private final RiderRepository riderRepo;

    public RideServiceImpl(
            RideRepository rideRepo,DriverRepository driverRepo,RiderRepository riderRepo){
        this.rideRepo=rideRepo;
        this.driverRepo=driverRepo;
        this.riderRepo=riderRepo;
    }
    public Ride requestRide(Long driverId,Long riderId){
        Driver driver= driverRepo.findById(driverId).orElseThrow(()->new RuntimeException("Driver not found"));
        Rider rider= riderRepo.findById(riderId).orElseThrow(()->new RuntimeException("Rider not found"));
        Ride ride=new Ride();
        ride.setRider(rider);
        ride.setDriver(driver);
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

    public Ride completeRide(Long id){
        Ride ride =findById(id);
        ride.setStatus(RideStatus.COMPLETED);
        return rideRepo.save(ride);
    }
    public List<Ride> findByStatus(RideStatus status){
        return rideRepo.findByStatus(status);
    }
}
