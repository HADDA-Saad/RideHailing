package com.ridehailing.PriceEstimate.controller;

import com.ridehailing.PriceEstimate.dto.PriceEstimateRequest;
import com.ridehailing.PriceEstimate.dto.PriceEstimateResponse;
import com.ridehailing.PriceEstimate.service.PriceEstimateService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/prices")
public class PriceEstimateController{
    private final PriceEstimateService service;
    public PriceEstimateController(PriceEstimateService service){this.service=service;}
    @PostMapping
    public PriceEstimateResponse calculatePrice(@RequestBody PriceEstimateRequest request){
        return PriceEstimateResponse.from(service.calculatePrice(request.rideId(),request.distanceKm()));
    }

    //for previous estimate prices
    @GetMapping("/{rideId}")
    public PriceEstimateResponse findByRideId(@PathVariable Long rideId){
        return PriceEstimateResponse.from(service.findByRideId(rideId));
    }
}
