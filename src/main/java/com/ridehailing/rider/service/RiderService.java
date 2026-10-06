package com.ridehailing.rider.service;

import com.ridehailing.rider.model.Rider;

import java.util.List;

public interface RiderService {
    Rider register(String name,String email);
    Rider findById(Long id);
    Rider findByEmail(String email);
    List<Rider> findByName(String name);
}
