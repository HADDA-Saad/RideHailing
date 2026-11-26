package com.ridehailing.location.service;

import org.springframework.security.access.prepost.PreAuthorize;
import com.ridehailing.location.dto.DriverLocationRequest;
import com.ridehailing.location.model.DriverLocation;
import java.util.List;


public interface DriverLocationService {
    DriverLocation saveLocation(Long driverId, double lat, double lng);
    DriverLocation getLatest(Long driverId);
    @PreAuthorize("hasRole('DRIVER')")
    void publishLocation(DriverLocationRequest request, Long userId);

    @PreAuthorize("hasRole('DRIVER')")
    List<DriverLocation> getHistory(Long driverId, Long userId);
}
