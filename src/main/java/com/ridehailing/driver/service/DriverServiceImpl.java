package com.ridehailing.driver.service;

import com.ridehailing.driver.model.Driver;
import com.ridehailing.driver.repository.DriverRepository;
import org.springframework.stereotype.Service;

@Service
public class DriverServiceImpl implements DriverService{
    private final DriverRepository repo;
    public DriverServiceImpl(DriverRepository repo){
        this.repo=repo;
    }
    @Override
    public Driver register(String name, String email){
        Driver driver=new Driver();
        driver.setName(name);
        driver.setEmail(email);
        driver.setAvailable(true);
        return repo.save(driver);
    }

    @Override
    public Driver findById(Long id) {
        return repo.findById(id).
                orElseThrow(()->new RuntimeException("Driver not found"));
    }

    @Override
    public Driver findByEmail(String email){
        return repo.findByEmail(email).
                orElseThrow(()->new RuntimeException("Driver not found"));
    }

    @Override
    public void setAvailability(Long id,boolean available){
        Driver driver= findById(id);
        driver.setAvailable(available);
        repo.save(driver);
    }
}
