package com.ridehailing.ride.service;

import com.ridehailing.ride.model.Ride;
import com.ridehailing.ride.model.RideStatus;
import com.ridehailing.ride.repository.RideRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RideServiceImplTest {
    @Mock
    RideRepository rideRepo;

    @InjectMocks
    RideServiceImpl service;

    private void rideRepoEchoesSaved(){
        when(rideRepo.save(any(Ride.class))).thenAnswer(inv->inv.getArgument(0));
    }

    private Ride rideWithStatus(RideStatus status) {
        Ride ride = new Ride();
        ride.setDriverId(1L);
        ride.setRiderId(2L);
        ride.setStatus(status);
        return ride;
    }


    @Test
    @DisplayName("requestRide: creates the ride with status ACTIVE")
    void requestRide_createsRideWithStatusActive() {
        rideRepoEchoesSaved();
        Ride result = service.requestRide(10L, 20L);
        assertThat(result.getStatus()).isEqualTo(RideStatus.ACTIVE);
    }


    @Test
    void findById() {
    }

    @Test
    void findByDriverId_delegatesToRepository() {
        List<Ride> rides = List.of(rideWithStatus(RideStatus.ACTIVE));
        when(rideRepo.findByDriverId(10L)).thenReturn(rides);
        assertThat(service.findByDriverId(10L)).isSameAs(rides);
    }

    @Test
    void findByRiderId() {
    }

    @ParameterizedTest(name = "a {0} ride can still be completed")
    @EnumSource(value = RideStatus.class,
            names = {"WAITING", "ACTIVE", "COMPLETED", "CANCELLED"})
    @DisplayName("KNOWN ISSUE completeRide: any status can be completed, with notransition rules")
    void completeRide_acceptsAnyStartingStatus_knownIssue(RideStatus startingStatus
    ) {
        Ride ride = rideWithStatus(startingStatus);
        when(rideRepo.findById(1L)).thenReturn(Optional.of(ride));
        rideRepoEchoesSaved();
        Ride result = service.completeRide(1L);
        assertThat(result.getStatus()).isEqualTo(RideStatus.COMPLETED);
    }


    @Test
    void findByStatus() {
    }
}