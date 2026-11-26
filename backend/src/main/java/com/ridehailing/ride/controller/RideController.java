package com.ridehailing.ride.controller;

import org.springframework.security.core.Authentication;
import com.ridehailing.auth.model.Role;

import com.ridehailing.ride.dto.RideRequest;
import com.ridehailing.ride.dto.RideResponse;
import com.ridehailing.ride.model.Ride;
import com.ridehailing.ride.model.RideStatus;
import com.ridehailing.ride.service.RideService;
import com.ridehailing.auth.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/rides")
public class RideController {
    private final RideService service;

    public RideController(RideService service) {
        this.service = service;
    }

    @PostMapping
    public RideResponse requestRide(@Valid @RequestBody RideRequest request,
                                    @AuthenticationPrincipal AuthenticatedUser user) {
        Ride ride = service.requestRide(request.driverId(), user.userId());
        return RideResponse.from(ride);
    }

    @PatchMapping("/{id}/complete")
    public RideResponse completeRide(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        Ride ride = service.completeRide(id, user.userId());
        return RideResponse.from(ride);
    }

    @GetMapping("/driver/{driverId}")
    public List<RideResponse> findByDriverId(@PathVariable Long driverId,
            @AuthenticationPrincipal AuthenticatedUser me) {
        List<Ride> rides = service.findByDriverId(driverId, me.userId());
        List<RideResponse> result = new ArrayList<>();
        for (Ride ride : rides) {
            result.add(RideResponse.from(ride));
        }
        return result;
    }

    @GetMapping("/rider/{riderId}")
    public List<RideResponse> findByRiderId(@PathVariable Long riderId,
            @AuthenticationPrincipal AuthenticatedUser me) {
        List<Ride> rides = service.findByRiderId(riderId, me.userId());
        List<RideResponse> result = new ArrayList<>();
        for (Ride ride : rides) {
            result.add(RideResponse.from(ride));
        }
        return result;
    }

    @GetMapping("/{id}")
    public RideResponse findById(@PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser me, Authentication authentication) {
        Ride ride = service.findById(id, me.userId(), Role.fromAuthentication(authentication));
        return RideResponse.from(ride);
    }

    @GetMapping("/status/{status}")
    public List<RideResponse> findByStatus(@PathVariable RideStatus status,
            @AuthenticationPrincipal AuthenticatedUser me, Authentication authentication) {
        List<Ride> rides = service.findByStatus(status, me.userId(), Role.fromAuthentication(authentication));
        List<RideResponse> result = new ArrayList<>();
        for (Ride ride : rides) {
            result.add(RideResponse.from(ride));
        }

        return result;

    }
}
