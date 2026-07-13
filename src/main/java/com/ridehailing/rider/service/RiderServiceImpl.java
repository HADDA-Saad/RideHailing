package com.ridehailing.rider.service;

import com.ridehailing.driver.repository.DriverRepository;
import com.ridehailing.ride.repository.RideRepository;
import com.ridehailing.rider.model.Rider;
import com.ridehailing.rider.repository.RiderRepository;
import org.springframework.stereotype.Service;

@Service
public class RiderServiceImpl implements RiderService{
    private final RiderRepository repo;
    public RiderServiceImpl(RiderRepository repo){this.repo=repo;}
    public Rider register(String name, String email){
        Rider rider=new Rider();
        rider.setName(name);
        rider.setEmail(email);
        return repo.save(rider);
    }
    public Rider findById(Long id){
        return repo.findById(id).
                orElseThrow(()->new RuntimeException("Rider not found"));
    }

    public Rider findByEmail(String email){
        return repo.findByEmail(email).
                orElseThrow(()->new RuntimeException("Rider not found"));
    }
}
