package com.ridehailing.location.service;

import com.ridehailing.location.dto.DriverLocationRequest;
import com.ridehailing.location.model.DriverLocation;
import com.ridehailing.location.repository.DriverLocationRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

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
        location.setTimestamp(LocalDate.now());
        return repo.save(location);
    }

    public DriverLocation getLatest(Long driverId){
        String key="driver:latest:" + driverId;
        String cached=redisTemplate.opsForValue().get(key);
        if(cached!=null) {
            try {
                DriverLocationRequest req = objectMapper.readValue(cached, DriverLocationRequest.class);
                return repo.findTopByDriverIdOrderByTimestampDesc(req.driverId()).orElseThrow(() -> new RuntimeException("Location not found"));
            }catch (Exception ignored){}
        }
        DriverLocation location=repo
                .findTopByDriverIdOrderByTimestampDesc(driverId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        try{
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(location));
        }catch (Exception ignored){}
        return location;
    }

     public List<DriverLocation> getHistory(Long driverId){
        return repo.findByDriverId(driverId);
     }
}
