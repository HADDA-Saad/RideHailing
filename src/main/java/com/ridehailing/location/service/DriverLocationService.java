package com.ridehailing.location.service;
import com.ridehailing.location.model.DriverLocation;
import org.springframework.stereotype.Service;
import java.util.List;


public interface DriverLocationService {
    DriverLocation saveLocation(Long driverId, double lat, double lng);
    DriverLocation getLatest(Long driverId);
    List<DriverLocation> getHistory(Long driverId);
}
