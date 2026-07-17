package com.ridehailing.ride.controller;

import com.ridehailing.ride.dto.RideRequest;
import com.ridehailing.ride.dto.RideResponse;
import com.ridehailing.ride.model.Ride;
import com.ridehailing.ride.model.RideStatus;
import com.ridehailing.ride.service.RideService;
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
    public RideResponse requestRide(@RequestBody RideRequest request) {
        Ride ride = service.requestRide(request.driverId(), request.riderId());
        return RideResponse.from(ride);
    }

    @PatchMapping("/{id}/complete")
    public RideResponse completeRide(@PathVariable Long id) {
        Ride ride = service.completeRide(id);
        return RideResponse.from(ride);
    }

    @GetMapping("/driver/{driverId}")
    public List<RideResponse> findByDriverId(@PathVariable Long driverId) {
        List<Ride> rides = service.findByDriverId(driverId);
        List<RideResponse> result = new ArrayList<>();
        for (Ride ride : rides) {
            result.add(RideResponse.from(ride));
        }
        return result;
    }

    @GetMapping("/rider/{riderId}")
    public List<RideResponse> findByRiderId(@PathVariable Long riderId) {
        List<Ride> rides = service.findByRiderId(riderId);
        List<RideResponse> result = new ArrayList<>();
        for (Ride ride : rides) {
            result.add(RideResponse.from(ride));
        }
        return result;
    }

    @GetMapping("/{id}")
    public RideResponse findById(@PathVariable Long id) {
        Ride ride = service.findById(id);
        return RideResponse.from(ride);
    }

    @GetMapping("/status/{status}")
    public List<RideResponse> findByStatus(@PathVariable RideStatus status) {
        List<Ride> rides = service.findByStatus(status);
        List<RideResponse> result = new ArrayList<>();
        for (Ride ride : rides) {
            result.add(RideResponse.from(ride));
        }

        return result;

    }
}
