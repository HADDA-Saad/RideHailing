package com.ridehailing.auth.controller;

import com.ridehailing.auth.dto.LoginRequest;
import com.ridehailing.auth.dto.LoginResponse;
import com.ridehailing.auth.dto.RegisterDriverRequest;
import com.ridehailing.auth.dto.RegisterRiderRequest;
import com.ridehailing.auth.dto.RegisterResponse;
import com.ridehailing.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register/rider")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse registerRider(@Valid @RequestBody RegisterRiderRequest request) {
        Long userId = authService.registerRider(request);
        return new RegisterResponse(userId, request.email());
    }

    @PostMapping("/register/driver")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse registerDriver(@Valid @RequestBody RegisterDriverRequest request) {
        Long userId = authService.registerDriver(request);
        return new RegisterResponse(userId, request.email());
    }
}
