package com.ridehailing.ride.service;

import com.ridehailing.ride.model.Ride;
import com.ridehailing.ride.model.RideStatus;
import com.ridehailing.ride.repository.RideRepository;
import org.springframework.stereotype.Service;


@Service
public class RideServiceImpl implements RideService{
    private final RideRepository repo;
    public RideServiceImpl(RideRepository repo){this.repo=repo;}
    public Ride RequestRide(Long driverId,Long riderId){
        Ride ride=new Ride();
        ride.setRider(ride.getRider());
        ride.setDriver(ride.getDriver());
        ride.setStatus(RideStatus.COMPLETED);
        return repo.save(ride);
    }
    public Ride findById(Long id){
        return repo.findById(id).
                orElseThrow(()->new RuntimeException("Ride not found"));
    }
    public Ride completeRide(Long id){
        Ride ride =findById(id);
        ride.setStatus(RideStatus.COMPLETED);
        return ride;
    }

}
