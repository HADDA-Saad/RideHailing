package com.ridehailing.location.service;

import com.ridehailing.location.model.DriverLocation;
import com.ridehailing.location.repository.DriverLocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverLocationServiceImplTest {
    private static final String KEY = "driver:latest:7";

    @Mock DriverLocationRepository repo;
    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> valueOps;
    ObjectMapper objectMapper = JsonMapper.builder().build();

    DriverLocationServiceImpl service;
    @BeforeEach
    void setUp() {
        service = new DriverLocationServiceImpl(repo, redisTemplate, objectMapper);
    }
    private void cacheReturns(String json) {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(KEY)).thenReturn(json);
    }
    private DriverLocation location(long id, double lat, double lng) {
        DriverLocation loc = new DriverLocation();
        loc.setId(id);
        loc.setDriverId(7L);
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        loc.setTimestamp(LocalDateTime.of(2026, 10, 6, 19, 0));
        return loc;
    }


    @Test
    void saveLocation() {
    }

    @Test
    @DisplayName("getLatest: cache hit returns the cached location and skips the database")
    void getLatest_returnsCachedLocation_onCacheHit() {
        String json = objectMapper.writeValueAsString(location(1L, 36.75, 3.06));
        cacheReturns(json);
        DriverLocation result = service.getLatest(7L);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLatitude()).isEqualTo(36.75);
        assertThat(result.getTimestamp()).isEqualTo(LocalDateTime.of(2026, 10, 6, 19, 0));
        verifyNoInteractions(repo);
        verify(valueOps, never()).set(anyString(), anyString(), any(Duration.class)
        );
    }

    @Test
    @DisplayName("getLatest: cache miss reads the database and caches the result for 24 hours")
    void getLatest_readsDatabaseAndCaches_onCacheMiss() {
        cacheReturns(null);
        when(repo.findTopByDriverIdOrderByTimestampDesc(7L))
                .thenReturn(Optional.of(location(1L, 36.75, 3.06)));
        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        DriverLocation result = service.getLatest(7L);
        assertThat(result.getId()).isEqualTo(1L);
        verify(valueOps).set(eq(KEY), json.capture(), eq(Duration.ofHours(24)));
        DriverLocation cached = objectMapper.readValue(json.getValue(), DriverLocation.class);
        assertThat(cached.getLatitude()).isEqualTo(36.75);
        assertThat(cached.getLongitude()).isEqualTo(3.06);
    }

    @Test
    @DisplayName("getLatest: cache miss and no row throws 'Location not found' and caches nothing")
    void getLatest_throwsAndCachesNothing_whenNoLocationExists() {
        cacheReturns(null);
        when(repo.findTopByDriverIdOrderByTimestampDesc(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLatest(7L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Location not found");
        verify(valueOps, never()).set(anyString(), anyString(), any(Duration.class)
        );
    }

    @Test
    @DisplayName("KNOWN ISSUE getLatest: corrupt cache entry is silently ignored and overwritten")
    void getLatest_silentlyFallsBackToDatabase_whenCacheIsCorrupt_knownIssue() {
        cacheReturns("{this is not json");
        when(repo.findTopByDriverIdOrderByTimestampDesc(7L))
                .thenReturn(Optional.of(location(1L, 36.75, 3.06)));
        DriverLocation result = service.getLatest(7L);
        assertThat(result.getId()).isEqualTo(1L);
        verify(valueOps).set(eq(KEY), anyString(), eq(Duration.ofHours(24)));
    }

    @Test
    @DisplayName("KNOWN ISSUE getLatest: a Redis WRITE failure is swallowed")
    void getLatest_swallowsRedisWriteFailure_knownIssue() {
        cacheReturns(null);
        when(repo.findTopByDriverIdOrderByTimestampDesc(7L))
                .thenReturn(Optional.of(location(1L, 36.75, 3.06)));
        doThrow(new RedisConnectionFailureException("Redis is down"))
                .when(valueOps).set(anyString(), anyString(), any(Duration.class));
        DriverLocation result = service.getLatest(7L);
        assertThat(result.getId()).isEqualTo(1L);
    }




    @Test
    @DisplayName("saveLocation: builds the entity from the arguments and saves it")
    void saveLocation_buildsEntityAndSavesIt() {
        when(repo.save(any(DriverLocation.class)))
                .thenAnswer(inv->inv.getArgument(0));
        ArgumentCaptor<DriverLocation> captor=ArgumentCaptor.forClass(DriverLocation.class);
        service.saveLocation(1L, 36.75, 3.06);

        verify(repo).save(captor.capture());

        DriverLocation saved = captor.getValue();
        assertThat(saved.getDriverId()).isEqualTo(1L);
        assertThat(saved.getLatitude()).isEqualTo(36.75);
        assertThat(saved.getLongitude()).isEqualTo(3.06);
        assertThat(saved.getTimestamp()).isNull();
    }

    @Test
    void getHistory() {

    }

    @Test
    void testGetLatest() {
    }

    @Test
    void testGetHistory() {
    }
}