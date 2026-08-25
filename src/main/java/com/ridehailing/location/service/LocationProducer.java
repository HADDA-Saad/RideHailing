package com.ridehailing.location.service;

import com.ridehailing.location.dto.DriverLocationRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class LocationProducer {
    private static final String TOPIC = "driver-locations";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    public LocationProducer(KafkaTemplate<String, String> kafka,
                            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafka;
        this.objectMapper = objectMapper;
    }
    public void publish(DriverLocationRequest req) {
        try {
            String payload = objectMapper.writeValueAsString(req);
            kafkaTemplate.send(TOPIC, req.driverId().toString(), payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish location event", e);
        }
    }
}
