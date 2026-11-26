package com.ridehailing.location.service;

import com.ridehailing.location.dto.DriverLocationRequest;
import com.ridehailing.driver.repository.DriverRepository;
import com.ridehailing.driver.model.Driver;
import org.springframework.security.access.AccessDeniedException;

import com.ridehailing.location.model.DriverLocation;
import com.ridehailing.location.repository.DriverLocationRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;

@Service
public class DriverLocationServiceImpl implements DriverLocationService {
    private final DriverLocationRepository repo;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private final DriverRepository drivers;
    private final LocationProducer producer;

    public DriverLocationServiceImpl(DriverLocationRepository repo, RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper, DriverRepository drivers, LocationProducer producer) {
        this.drivers = drivers;
        this.producer = producer;
        this.repo = repo;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public DriverLocation saveLocation(Long driverId, double lat, double lng) {
        DriverLocation location = new DriverLocation();
        location.setDriverId(driverId);
        location.setLatitude(lat);
        location.setLongitude(lng);
        return repo.save(location);
    }

    public DriverLocation getLatest(Long driverId) {
        String key = "driver:latest:" + driverId;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                System.out.println("[CACHE] Fetched DriverLocation for driver " + driverId + " from REDIS");
                return objectMapper.readValue(cached, DriverLocation.class);
            } catch (Exception ignored) {
            }
        }
        System.out.println("[DB] Fetched DriverLocation for driver " + driverId + " from POSTGRESQL");
        DriverLocation location = repo
                .findTopByDriverIdOrderByTimestampDesc(driverId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(location), Duration.ofHours(24));
        } catch (Exception ignored) {
        }
        return location;
    }

    private Long requireOwnDriverId(Long requestedId, Long userId) {
        return drivers.findByUserId(userId).map(Driver::getId)
                .filter(id -> id.equals(requestedId))
                .orElseThrow(() -> new AccessDeniedException("Own driver profile required"));
    }

    @Override
    public void publishLocation(DriverLocationRequest request, Long userId) {
        Long driverId = requireOwnDriverId(request.driverId(), userId);
        producer.publish(new DriverLocationRequest(driverId, request.latitude(), request.longitude()));
    }

    @Override
    public List<DriverLocation> getHistory(Long driverId, Long userId) {
        return repo.findByDriverId(requireOwnDriverId(driverId, userId));
    }
}
