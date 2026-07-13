package com.ridehailing.driver.service;

import com.ridehailing.driver.model.Driver;

public interface DriverService {
    Driver register(String name, String email);
    Driver findById(Long id);
    Driver findByEmail(String email);
    void setAvailability (Long id,boolean available);

}
