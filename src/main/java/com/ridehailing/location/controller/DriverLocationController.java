package com.ridehailing.location.controller;


import com.ridehailing.location.dto.DriverLocationRequest;
import com.ridehailing.location.dto.DriverLocationResponse;
import com.ridehailing.location.model.DriverLocation;
import com.ridehailing.location.service.DriverLocationService;
import com.ridehailing.location.service.LocationProducer;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
public class DriverLocationController {
    private final DriverLocationService service;
    private final LocationProducer producer;
    public DriverLocationController(DriverLocationService service,LocationProducer producer){
        this.service=service;
        this.producer=producer;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void ingestLocation(@RequestBody DriverLocationRequest request){
         producer.publish(request);
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
