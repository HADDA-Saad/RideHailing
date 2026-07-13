package com.ridehailing.rider.service;

import com.ridehailing.rider.model.Rider;

public interface RiderService {
    Rider register(String name,String email);
    Rider findById(Long id);
    public Rider findByEmail(String email);
}
