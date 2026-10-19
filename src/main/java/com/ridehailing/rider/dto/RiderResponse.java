package com.ridehailing.rider.dto;

import com.ridehailing.rider.model.Rider;

public record RiderResponse(Long id, String name, String email, String phone) {
    public static RiderResponse from(Rider r){
        return new RiderResponse(
                r.getId(), r.getName(), r.getEmail(), r.getPhone());
    }
}
