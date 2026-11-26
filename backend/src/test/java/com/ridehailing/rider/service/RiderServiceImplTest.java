package com.ridehailing.rider.service;

import com.ridehailing.rider.dto.RiderSummaryResponse;
import com.ridehailing.driver.dto.DriverSummaryResponse;
import tools.jackson.databind.json.JsonMapper;

import com.ridehailing.auth.model.Role;
import org.springframework.security.access.AccessDeniedException;
import com.ridehailing.driver.model.Driver;
import com.ridehailing.driver.repository.DriverRepository;
import com.ridehailing.ride.model.RideStatus;
import com.ridehailing.ride.repository.RideRepository;

import com.ridehailing.rider.model.Rider;
import com.ridehailing.rider.repository.RiderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiderServiceImplTest {

    @Mock
    RiderRepository repo;

    @Mock DriverRepository drivers;
    @Mock RideRepository rides;

    @InjectMocks
    RiderServiceImpl service;

    @Test
    @DisplayName("register: hands a fully built Rider to the repository")
    void register_passesBuiltRiderToRepository() {
        when(repo.save(any(Rider.class)))
                .thenAnswer(inv-> inv.getArgument(0));
        ArgumentCaptor<Rider> captor=ArgumentCaptor.forClass(Rider.class);

        service.register(1L, "Ema", "Ema@gmail.com", "0600000000");
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Ema");
        assertThat(captor.getValue().getEmail()).isEqualTo("Ema@gmail.com");
        //cause the object never reaches the DB so the id is never generated therefore if the id isn't null we re working on a pre-existing object
        assertThat(captor.getValue().getId()).isNull();
    }

    @Test
    @DisplayName("KNOWN ISSUE register: accepts null name and email without validation")
    void register_acceptsNulls_knownIssue() {
        when(repo.save(any(Rider.class))).
                thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<Rider> captor = ArgumentCaptor.forClass(Rider.class);

        Rider result = service.register(null, null, null, null);

        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getName()).isNull();
        assertThat(captor.getValue().getEmail()).isNull();
        assertThat(result).isNotNull();
    }


    @Test
    void  findById_returnsRider_whenPresent() {
        Rider rider =new Rider();
        when(repo.findById(1L)).thenReturn(Optional.of(rider));

        Rider result=service.findById(1L);

        assertThat(result).isSameAs(rider);
    }
    @Test
    void  findById_throws_whenMissing() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(()->service.findById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Rider not found");
    }
    @Test
    void  findByEmail_throws_whenMissing() {
        when(repo.findByEmail("Ema@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(()->service.findByEmail("Ema@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Rider not found");
    }

    @Test
    void findByEmail_when_present() {
        Rider rider =new Rider();
        when(repo.findByEmail("Sam@example.com")).
                thenReturn(Optional.of(rider));

        Rider result=service.findByEmail("Sam@example.com");

        assertThat(result.getEmail()).isEqualTo(rider.getEmail());
    }

    @Test
    void riderCanReadOnlyTheirOwnSummary() {
        Rider rider = new Rider();
        rider.setId(7L);
        when(repo.findByUserId(42L)).thenReturn(Optional.of(rider));
        assertThat(service.findVisibleById(7L, 42L, Role.RIDER)).isSameAs(rider);
        assertThatThrownBy(() -> service.findVisibleById(42L, 42L, Role.RIDER))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void driverNeedsAnActiveRideWithTheRequestedRider() {
        Driver driver = new Driver();
        driver.setId(8L);
        when(drivers.findByUserId(43L)).thenReturn(Optional.of(driver));
        when(rides.existsByDriverIdAndRiderIdAndStatus(8L, 7L, RideStatus.ACTIVE)).thenReturn(true);
        Rider rider = new Rider();
        when(repo.findById(7L)).thenReturn(Optional.of(rider));
        assertThat(service.findVisibleById(7L, 43L, Role.DRIVER)).isSameAs(rider);
        assertThatThrownBy(() -> service.findVisibleById(9L, 43L, Role.DRIVER))
                .isInstanceOf(AccessDeniedException.class);
        verify(repo, never()).findById(9L);
    }

    @Test
    void profileSummariesExposeOnlyApprovedFields() {
        JsonMapper mapper = JsonMapper.builder().build();
        assertThat(mapper.valueToTree(RiderSummaryResponse.from(new Rider())).propertyNames())
                .containsExactlyInAnyOrder("id", "name");
        assertThat(mapper.valueToTree(DriverSummaryResponse.from(new Driver())).propertyNames())
                .containsExactlyInAnyOrder("id", "name", "available");
    }
}
