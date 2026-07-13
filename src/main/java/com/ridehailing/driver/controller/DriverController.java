package com.ridehailing.driver.controller;

import com.ridehailing.driver.dto.DriverResponse;
import com.ridehailing.driver.dto.RegisterDriverRequest;
import com.ridehailing.driver.model.Driver;
import com.ridehailing.driver.service.DriverService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/drivers")
public class DriverController {
    public final DriverService service;

    public DriverController(DriverService service){
        this.service=service;
    }
    @PostMapping
    public DriverResponse register(
            @RequestBody RegisterDriverRequest request
    ){
        var driver=service.register(
                request.name(),request.email());
        return DriverResponse.from(driver);
    }
    @GetMapping("/{id}")
    public DriverResponse FindById(
            @PathVariable Long id){
        var driver=service.findById(id);
        return DriverResponse.from(driver);
    }
}
