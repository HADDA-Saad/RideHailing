package com.ridehailing.rider.controller;

import com.ridehailing.rider.dto.RiderResponse;
import com.ridehailing.rider.model.Rider;
import com.ridehailing.rider.service.RiderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/riders")
public class RiderController {
    public final RiderService service;

    public RiderController(RiderService service){
        this.service=service;
    }

    @GetMapping("/{id}")
    public RiderResponse findById(@PathVariable Long id){
        Rider rider=service.findById(id);
        return RiderResponse.from(rider);
    }

    @GetMapping
    public RiderResponse findByEmail(@RequestParam String email){
        Rider rider=service.findByEmail(email);
        return RiderResponse.from(rider);
    }
}
