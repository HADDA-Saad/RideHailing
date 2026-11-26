package com.ridehailing.location.controller;

import com.ridehailing.auth.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


import com.ridehailing.location.dto.DriverLocationRequest;
import com.ridehailing.location.dto.DriverLocationResponse;
import com.ridehailing.location.model.DriverLocation;
import com.ridehailing.location.service.DriverLocationService;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void ingestLocation(@RequestBody DriverLocationRequest request, @AuthenticationPrincipal AuthenticatedUser me) {
        service.publishLocation(request, me.userId());
    }

    @GetMapping("/driver/{driverId}")
    public DriverLocationResponse getLatest(@PathVariable Long driverId){
        return DriverLocationResponse.from(service.getLatest(driverId));
    }
    @GetMapping("/history/{driverId}")
    List<DriverLocationResponse> getHistory(@PathVariable Long driverId, @AuthenticationPrincipal AuthenticatedUser me){
        return service.getHistory(driverId, me.userId()).stream().map(DriverLocationResponse::from).toList();
    }


}
