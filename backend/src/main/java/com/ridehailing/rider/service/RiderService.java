package com.ridehailing.rider.service;

import com.ridehailing.auth.model.Role;
import org.springframework.security.access.prepost.PreAuthorize;

import com.ridehailing.rider.model.Rider;

import java.util.List;

public interface RiderService {
    Rider register(Long userId, String name, String email, String phone);
    @PreAuthorize("hasAnyRole('RIDER', 'DRIVER')")
    Rider findVisibleById(Long id, Long userId, Role role);
    Rider findById(Long id);
    Rider findByEmail(String email);
    Rider findByUserId(Long userId);
    Rider updateProfile(Long userId, String name, String phone);
    List<Rider> findByName(String name);
}
