package com.ridehailing.ride.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.ridehailing.auth.config.SecurityConfig;
import com.ridehailing.auth.security.*;
import com.ridehailing.driver.model.Driver;
import com.ridehailing.driver.repository.DriverRepository;
import com.ridehailing.ride.model.Ride;
import com.ridehailing.ride.model.RideStatus;
import com.ridehailing.ride.repository.RideRepository;
import com.ridehailing.ride.service.RideServiceImpl;
import com.ridehailing.rider.repository.RiderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RideController.class, properties = {
        "jwt.secret=RkZGRkZGRkZGRkZGRkZGRkZGRkZGRkZGRkZGRkZGRkY=", "jwt.expiration=60000"})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class,
        RestAccessDeniedHandler.class, RestAuthenticationEntryPoint.class, RideServiceImpl.class})
class CompleteRideHttpTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }
    @Autowired JwtService jwt;
    @MockitoBean DriverRepository drivers;
    @MockitoBean RideRepository rides;
    @MockitoBean RiderRepository riders;
    @MockitoBean TokenDenylistService denylist;

    private MockHttpServletRequestBuilder requestAs(String role) {
        String token = jwt.generateToken("member@example.test", 42L, role);
        return patch("/rides/9/complete").header("Authorization", "Bearer " + token);
    }

    private void driverProfile() {
        Driver driver = new Driver();
        driver.setId(7L);
        when(drivers.findByUserId(42L)).thenReturn(Optional.of(driver));
    }

    private Ride assignedTo(long driverId) {
        Ride ride = new Ride();
        ride.setId(9L);
        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ACTIVE);
        when(rides.findById(9L)).thenReturn(Optional.of(ride));
        return ride;
    }

    @Test
    void assignedDriverCompletesRide() throws Exception {
        driverProfile();
        Ride ride = assignedTo(7L);
        when(rides.save(ride)).thenReturn(ride);
        mvc.perform(requestAs("DRIVER")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        verify(rides).save(ride);
    }

    @Test
    void anotherDriverIsForbidden() throws Exception {
        driverProfile();
        Ride ride = assignedTo(42L);
        mvc.perform(requestAs("DRIVER")).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Access denied"));
        assertThat(ride.getStatus()).isEqualTo(RideStatus.ACTIVE);
        verify(rides, never()).save(any());
    }

    @Test
    void riderIsForbiddenBeforeLookup() throws Exception {
        mvc.perform(requestAs("RIDER")).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Access denied"));
        verifyNoInteractions(drivers, rides);
    }

    @Test
    void missingRideIsNotFound() throws Exception {
        driverProfile();
        mvc.perform(requestAs("DRIVER")).andExpect(status().isNotFound());
        verify(rides).findById(9L);
        verify(rides, never()).save(any());
    }

    @Test
    void missingDriverProfileIsForbidden() throws Exception {
        mvc.perform(requestAs("DRIVER")).andExpect(status().isForbidden());
        verifyNoInteractions(rides);
    }

    @Test
    void missingAuthenticationIsUnauthorized() throws Exception {
        mvc.perform(patch("/rides/9/complete")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication required"));
        verifyNoInteractions(drivers, rides);
    }

    @Test
    void invalidAuthenticationIsUnauthorized() throws Exception {
        mvc.perform(patch("/rides/9/complete").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication required"));
        verifyNoInteractions(drivers, rides);
    }

    @Test
    void driverCanReadTheirRideThroughTheSecurityChain() throws Exception {
        driverProfile();
        assignedTo(7L);
        String token = jwt.generateToken("driver@example.test", 42L, "DRIVER");
        mvc.perform(get("/rides/9").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.driverId").value(7));
    }

    @Test
    void driverHistoryRequiresAuthenticationAndDriverRole() throws Exception {
        mvc.perform(get("/rides/driver/7")).andExpect(status().isUnauthorized());
        String token = jwt.generateToken("rider@example.test", 42L, "RIDER");
        mvc.perform(get("/rides/driver/7").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        verifyNoInteractions(drivers, riders, rides);
    }
}
