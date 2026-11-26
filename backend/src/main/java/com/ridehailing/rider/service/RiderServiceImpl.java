package com.ridehailing.rider.service;

import com.ridehailing.auth.model.Role;
import org.springframework.security.access.AccessDeniedException;
import com.ridehailing.driver.repository.DriverRepository;
import com.ridehailing.ride.repository.RideRepository;
import com.ridehailing.ride.model.RideStatus;


import com.ridehailing.rider.model.Rider;
import com.ridehailing.rider.repository.RiderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiderServiceImpl implements RiderService{
    private final RiderRepository repo;
    private final DriverRepository drivers;
    private final RideRepository rides;

    public RiderServiceImpl(RiderRepository repo, DriverRepository drivers, RideRepository rides) {
        this.repo = repo;
        this.drivers = drivers;
        this.rides = rides;
    }

    // An active ride lets the assigned driver identify the rider they are serving.
    @Override
    public Rider findVisibleById(Long id, Long userId, Role role) {
        if (role == Role.RIDER) {
            return repo.findByUserId(userId).filter(rider -> rider.getId().equals(id))
                    .orElseThrow(() -> new AccessDeniedException("Own rider profile required"));
        }
        boolean assigned = drivers.findByUserId(userId)
                .map(driver -> rides.existsByDriverIdAndRiderIdAndStatus(driver.getId(), id, RideStatus.ACTIVE))
                .orElse(false);
        if (!assigned) {
            throw new AccessDeniedException("Active ride with this rider required");
        }
        return findById(id);
    }

    public Rider register(Long userId, String name, String email, String phone){
        Rider rider=new Rider();
        rider.setUserId(userId);
        rider.setName(name);
        rider.setEmail(email);
        rider.setPhone(phone);
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

    @Override
    public Rider findByUserId(Long userId){
        return repo.findByUserId(userId).
                orElseThrow(()->new RuntimeException("Rider not found"));
    }

    @Override
    public Rider updateProfile(Long userId, String name, String phone){
        Rider rider=findByUserId(userId);
        rider.setName(name);
        rider.setPhone(phone);
        return repo.save(rider);
    }

    @Override
    public List<Rider> findByName(String name) {
        return repo.findByName(name);
    }
}
