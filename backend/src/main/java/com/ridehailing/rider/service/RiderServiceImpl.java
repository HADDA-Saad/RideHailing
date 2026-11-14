package com.ridehailing.rider.service;


import com.ridehailing.rider.model.Rider;
import com.ridehailing.rider.repository.RiderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiderServiceImpl implements RiderService{
    private final RiderRepository repo;
    public RiderServiceImpl(RiderRepository repo){this.repo=repo;}
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
