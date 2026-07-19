package com.ridehailing.location.controller;

import com.ridehailing.driver.model.Driver;
import com.ridehailing.driver.service.DriverService;
import com.ridehailing.location.dto.DriverLocationRequest;
import com.ridehailing.location.dto.DriverLocationResponse;
import com.ridehailing.location.model.DriverLocation;
import com.ridehailing.location.repository.DriverLocationRepository;
import com.ridehailing.location.service.DriverLocationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
public class DriverLocationController {
    private final DriverLocationService service;
    public DriverLocationController(DriverLocationService service){
        this.service=service;
    }
    @PostMapping
    public DriverLocationResponse saveLocation(@RequestBody DriverLocationRequest request){
        DriverLocation location=service.saveLocation(request.driverId(), request.latitude(),request.longitude());
        return DriverLocationResponse.from(location);
    }

    @GetMapping("/driver/{driverId}")
    public DriverLocationResponse getLatest(@PathVariable Long driverId){
        return DriverLocationResponse.from(service.getLatest(driverId));
    }
    @GetMapping("/history/{driverId}")
    List<DriverLocationResponse> getHistory(@PathVariable Long driverId){
        return service.getHistory(driverId).stream().map(DriverLocationResponse::from).toList();
    }


}
