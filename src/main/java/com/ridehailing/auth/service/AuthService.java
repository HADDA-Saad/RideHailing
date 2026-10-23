package com.ridehailing.auth.service;

import com.ridehailing.auth.repository.UserRepository;
import com.ridehailing.auth.dto.LoginRequest;
import com.ridehailing.auth.dto.LoginResponse;
import com.ridehailing.auth.dto.RegisterDriverRequest;
import com.ridehailing.auth.dto.RegisterRiderRequest;
import com.ridehailing.auth.exception.EmailAlreadyUsedException;
import com.ridehailing.auth.model.Role;
import com.ridehailing.auth.model.User;
import com.ridehailing.auth.security.JwtService;
import com.ridehailing.driver.service.DriverService;
import com.ridehailing.rider.service.RiderService;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final RiderService riderService;
    private final DriverService driverService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepo,
                       PasswordEncoder encoder,
                       RiderService riderService,
                       DriverService driverService,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.riderService = riderService;
        this.driverService = driverService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        String normalized = normalizeEmail(request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalized, request.password()));

        User user = userRepo.findByEmail(normalized)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + normalized));

        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole().name());
        return new LoginResponse(token, user.getId(), user.getRole().name());
    }

    @Transactional
    public Long registerRider(RegisterRiderRequest request) {
        Long userId = createAccount(request.email(), request.password(), Role.RIDER);
        riderService.register(userId, request.name(), normalizeEmail(request.email()), request.phone());
        return userId;
    }

    @Transactional
    public Long registerDriver(RegisterDriverRequest request) {
        Long userId = createAccount(request.email(), request.password(), Role.DRIVER);
        driverService.register(userId, request.name(), normalizeEmail(request.email()), request.phone());
        return userId;
    }

    private Long createAccount(String email, String rawPassword, Role role) {
        String normalized = normalizeEmail(email);
        if (userRepo.existsByEmail(normalized)) {
            throw new EmailAlreadyUsedException(normalized);
        }
        User u = new User();
        u.setEmail(normalized);
        u.setPassword(encoder.encode(rawPassword));
        u.setRole(role);
        return userRepo.save(u).getId();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
