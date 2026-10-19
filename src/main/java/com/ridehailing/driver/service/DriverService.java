package com.ridehailing.driver.service;

import com.ridehailing.driver.model.Driver;

public interface DriverService {
    Driver register(Long userId, String name, String email, String phone);
    Driver findById(Long id);
    Driver findByEmail(String email);
    void setAvailability (Long id,boolean available);

}
