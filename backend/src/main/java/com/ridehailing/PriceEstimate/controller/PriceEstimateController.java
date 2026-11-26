package com.ridehailing.PriceEstimate.controller;

import com.ridehailing.auth.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.ridehailing.auth.model.Role;

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
    public PriceEstimateResponse calculatePrice(@RequestBody PriceEstimateRequest request,
                                                @AuthenticationPrincipal AuthenticatedUser me){
        return PriceEstimateResponse.from(service.calculatePrice(request.rideId(),request.distanceKm(), me.userId()));
    }

    //for previous estimate prices
    @GetMapping("/{rideId}")
    public PriceEstimateResponse findByRideId(@PathVariable Long rideId, @AuthenticationPrincipal AuthenticatedUser me,
                                              Authentication authentication){
        return PriceEstimateResponse.from(service.findByRideId(rideId, me.userId(), Role.fromAuthentication(authentication)));
    }
}
