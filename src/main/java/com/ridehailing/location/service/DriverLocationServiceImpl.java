package com.ridehailing.location.service;

import com.ridehailing.location.model.DriverLocation;
import com.ridehailing.location.repository.DriverLocationRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DriverLocationServiceImpl implements DriverLocationService{
    private final DriverLocationRepository repo;
    private final RedisTemplate<String,String> redisTemplate;
    private final ObjectMapper objectMapper;
    public DriverLocationServiceImpl(DriverLocationRepository repo, RedisTemplate<String,String> redisTemplate, ObjectMapper objectMapper ){
        this.repo=repo;
        this.redisTemplate=redisTemplate;
        this.objectMapper=objectMapper;
    }
    public DriverLocation saveLocation(Long driverId, double lat, double lng){
        DriverLocation location=new DriverLocation();
        location.setDriverId(driverId);
        location.setLatitude(lat);
        location.setLongitude(lng);
        return repo.save(location);
    }

    public DriverLocation getLatest(Long driverId){
        String key="driver:latest:" + driverId;
        String cached=redisTemplate.opsForValue().get(key);
        if(cached!=null) {
            try {
                System.out.println("[CACHE] Fetched DriverLocation for driver " + driverId + " from REDIS");
                return objectMapper.readValue(cached, DriverLocation.class);
            }catch (Exception ignored){}
        }
        System.out.println("[DB] Fetched DriverLocation for driver " + driverId + " from POSTGRESQL");
        DriverLocation location=repo
                .findTopByDriverIdOrderByTimestampDesc(driverId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        try{
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(location), 24, TimeUnit.HOURS);
        }catch (Exception ignored){}
        return location;
    }

     public List<DriverLocation> getHistory(Long driverId){
        return repo.findByDriverId(driverId);
     }
}
