package com.ridehailing.driver.controller;

import com.ridehailing.auth.security.AuthenticatedUser;
import com.ridehailing.driver.dto.DriverResponse;
import com.ridehailing.driver.dto.UpdateDriverRequest;
import com.ridehailing.driver.model.Driver;
import com.ridehailing.driver.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/drivers")
public class DriverController {
    public final DriverService service;

    public DriverController(DriverService service){
        this.service=service;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    public DriverResponse getMe(@AuthenticationPrincipal AuthenticatedUser me){
        Driver driver=service.findByUserId(me.userId());
        return DriverResponse.from(driver);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    public DriverResponse updateMe(@AuthenticationPrincipal AuthenticatedUser me,
                                   @Valid @RequestBody UpdateDriverRequest request){
        Driver driver=service.updateProfile(me.userId(), request.name(), request.phone());
        return DriverResponse.from(driver);
    }

    @GetMapping("/{id}")
    public DriverResponse FindById(
            @PathVariable Long id){
        Driver driver=service.findById(id);
        return DriverResponse.from(driver);
    }
}
