package com.ridehailing.rider.service;

import com.ridehailing.rider.model.Rider;
import com.ridehailing.rider.repository.RiderRepository;
import org.hibernate.boot.internal.Extends;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiderServiceImplTest {

    @Mock
    RiderRepository repo;

    @InjectMocks
    RiderServiceImpl service;

    @DisplayName("register: hands a fully built Rider to the repository")
    void register_passesBuiltRiderToRepository() {
        when(repo.save(any(Rider.class)))
                .thenAnswer(inv-> inv.getArgument(0));
        ArgumentCaptor<Rider> captor=ArgumentCaptor.forClass(Rider.class);

        service.register("Ema","Ema@gmail.com");
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

        Rider result = service.register(null, null);

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

        assertThat(result.getEmail()).isSameAs(rider.getEmail());
    }
}