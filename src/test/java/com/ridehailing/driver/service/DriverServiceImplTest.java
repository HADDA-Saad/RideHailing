package com.ridehailing.driver.service;

import com.ridehailing.driver.model.Driver;
import com.ridehailing.driver.repository.DriverRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.Assert;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {

    @Mock
    DriverRepository repo;

    @InjectMocks
    DriverServiceImpl service;

    @Test
    @DisplayName("register: saves the driver and marks it available")
    void register_savesAvailableDriver() {
        //Arrange
        when(repo.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

        //Act
        Driver result=service.register("Sam","Sam@gmail.com");

        //Assert
        assertThat(result.getName()).isEqualTo("Sam");
        assertThat(result.getEmail()).isEqualTo("Sam@gmail.com");
        assertThat(result.isAvailable()).isTrue();
    }



    @Test
    void findById_returnsDriver_whenPresent() {
        Driver driver=new Driver();
        driver.setName("Sam");
        when(repo.findById(1L)).thenReturn(Optional.of(driver));

        Driver result=service.findById(1L);

        assertThat(result).isSameAs(driver);
    }
    @Test
    void findById_throws_whenMissing() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Driver not found");

    }

    @Test
    void setAvailability_doesNotSave_whenMissing() {
        when(repo.findById(42L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.setAvailability(42L, true))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Driver not found");
        verify(repo, never()).save(any(Driver.class));
    }

    @Test
    void setAvailability_isFlipping() {
        Driver driver=new Driver();
        driver.setAvailable(true);
        when(repo.findById(1L)).thenReturn(Optional.of(driver));
        ArgumentCaptor<Driver> captor= ArgumentCaptor.forClass(Driver.class);

        service.setAvailability(1L,false);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().isAvailable()).isFalse();
    }

    @Test
    void findByEmail_returnsDriver_whenPresent() {
        Driver driver=new Driver();
        driver.setEmail("Sam@gmail.com");
        when(repo.findByEmail("Sam@gmail.com")).thenReturn(Optional.of(driver));

        Driver result=service.findByEmail("Sam@gmail.com");
        assertThat(result.getEmail()).isSameAs(driver.getEmail());
    }
    
    @Test
    void findByEmail_throws_whenUnknown() {
        when(repo.findByEmail("Sam@gmail.com")).thenReturn(Optional.empty());
            assertThatThrownBy(()->service.findByEmail("Sam@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Driver not found");
    }
}