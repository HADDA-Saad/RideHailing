package com.ridehailing.driver.dto;

import com.ridehailing.driver.model.Driver;

public record DriverResponse (Long id, String name, String email, boolean available){
    public static DriverResponse from(Driver d){
        return new DriverResponse(
                d.getId(), d.getName(), d.getEmail(),d.isAvailable()
        );
    }
}
