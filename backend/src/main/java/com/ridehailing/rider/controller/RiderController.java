package com.ridehailing.rider.controller;

import com.ridehailing.auth.security.AuthenticatedUser;
import com.ridehailing.rider.dto.RiderResponse;
import com.ridehailing.rider.dto.UpdateRiderRequest;
import com.ridehailing.rider.model.Rider;
import com.ridehailing.rider.service.RiderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/riders")
public class RiderController {
    public final RiderService service;

    public RiderController(RiderService service){
        this.service=service;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('RIDER')")
    public RiderResponse getMe(@AuthenticationPrincipal AuthenticatedUser me){
        Rider rider=service.findByUserId(me.userId());
        return RiderResponse.from(rider);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('RIDER')")
    public RiderResponse updateMe(@AuthenticationPrincipal AuthenticatedUser me,
                                  @Valid @RequestBody UpdateRiderRequest request){
        Rider rider=service.updateProfile(me.userId(), request.name(), request.phone());
        return RiderResponse.from(rider);
    }

    @GetMapping("/{id}")
    public RiderResponse findById(@PathVariable Long id){
        Rider rider=service.findById(id);
        return RiderResponse.from(rider);
    }
}
