package com.ridehailing.location.service;

import com.ridehailing.location.dto.DriverLocationRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class LocationConsumer {
    private static final String REDIS_PREFIX = "driver:latest:";
    private final DriverLocationService service;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final LocationProducer producer;

    public LocationConsumer(DriverLocationService service,
                            RedisTemplate<String, String> redisTemplate,
                            ObjectMapper objectMapper,
                            LocationProducer producer) {
        this.service = service;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.producer = producer;
    }
    @KafkaListener(
            topics = "driver-locations",
            groupId = "location-persistence"
    )
    public void consume(String payload) {
        try {
            DriverLocationRequest req =objectMapper.readValue(payload, DriverLocationRequest.class);
            service.saveLocation(req.driverId(), req.latitude(), req.longitude());
            String key = REDIS_PREFIX + req.driverId();
            redisTemplate.opsForValue().set(key, payload);
        } catch (Exception e) {
            sendToDlq(payload, e);
        }
    }
    private void sendToDlq(String payload, Exception cause) {
        System.err.println("[DLQ] Unprocessable event: "
                + cause.getMessage() + " | payload: " + payload);
        producer.publishToDlq(payload);
    }

}

