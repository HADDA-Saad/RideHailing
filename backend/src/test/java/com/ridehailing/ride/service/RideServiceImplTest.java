package com.ridehailing.ride.service;

import com.ridehailing.ride.model.Ride;
import com.ridehailing.driver.model.Driver;
import com.ridehailing.driver.repository.DriverRepository;
import com.ridehailing.ride.model.RideStatus;
import com.ridehailing.ride.repository.RideRepository;
import com.ridehailing.rider.model.Rider;
import com.ridehailing.rider.repository.RiderRepository;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RideServiceImplTest {
    @Mock
    RideRepository rideRepo;
    @Mock
    DriverRepository driverRepo;
    @Mock
    RiderRepository riderRepo;

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
    void requestRide_savesActiveRideForResolvedRider() {
        Rider rider = new Rider();
        rider.setId(7L);
        when(riderRepo.findByUserId(42L)).thenReturn(Optional.of(rider));
        rideRepoEchoesSaved();

        Ride result = service.requestRide(10L, 42L);

        verify(rideRepo).save(result);
        assertThat(result.getRiderId()).isEqualTo(7L);
        assertThat(result.getDriverId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(RideStatus.ACTIVE);
    }

    @Test
    void requestRide_deniesMissingRiderProfileBeforeSave() {
        when(riderRepo.findByUserId(42L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.requestRide(10L, 42L));
        verify(rideRepo, never()).save(any());
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
        Driver driver = new Driver();
        driver.setId(ride.getDriverId());
        when(driverRepo.findByUserId(42L)).thenReturn(Optional.of(driver));
        Ride result = service.completeRide(1L, 42L);
        assertThat(result.getStatus()).isEqualTo(RideStatus.COMPLETED);
    }


    @Test
    void findByStatus() {
    }
}
