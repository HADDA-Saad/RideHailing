package com.ridehailing.location.service;

import com.ridehailing.location.model.DriverLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.KafkaException;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationConsumerTest {

    private static final String PAYLOAD = "{\"driverId\":7,\"latitude\":36,\"longitude\":3}";
    private static final String KEY = "driver:latest:7";
    private static final String WS_TOPIC = "/topic/driver-locations";

    @Mock DriverLocationService service;
    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> valueOps;
    @Mock SimpMessagingTemplate messagingTemplate;
    @Mock LocationProducer producer;
    @Captor ArgumentCaptor<String> jsonCaptor;
    ObjectMapper objectMapper = JsonMapper.builder().build();
    LocationConsumer consumer;
    @BeforeEach
    void setUp() {
        consumer = new LocationConsumer(service, redisTemplate, objectMapper,
                messagingTemplate, producer);
    }
    private DriverLocation savedLocation() {
        DriverLocation loc = new DriverLocation();
        loc.setId(42L);
        loc.setDriverId(7L);
        loc.setLatitude(36.0);
        loc.setLongitude(3.0);
        loc.setTimestamp(LocalDateTime.of(2026, 10, 3, 12, 0));
        return loc;
    }
    private void saveSucceeds() {
        when(service.saveLocation(7L, 36.0, 3.0)).thenReturn(savedLocation());
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    @DisplayName("consume: save, then cache, then broadcast -- in that order -- and never the DLQ")
    void consume_runsStepsInOrder_andNeverUsesDlq() {
        saveSucceeds();
        consumer.consume(PAYLOAD);
        InOrder order = inOrder(service, valueOps, messagingTemplate);
        order.verify(service).saveLocation(7L, 36.0, 3.0);
        order.verify(valueOps).set(eq(KEY),eq(PAYLOAD), Duration.ofHours(24));
        order.verify(messagingTemplate).convertAndSend(WS_TOPIC, PAYLOAD);
        verify(producer, never()).publishToDlq(anyString());
    }
    @Test
    @DisplayName("consume: caches the SAVED entity (with its id) under driver: latest:{id} for 24 hours")
    void consume_cachesSavedEntity() {
        saveSucceeds();
        consumer.consume(PAYLOAD);
        verify(valueOps).set(eq(KEY), jsonCaptor.capture(), eq(Duration.ofHours(24)));
        DriverLocation cached = objectMapper.readValue(jsonCaptor.getValue(), DriverLocation.class);
        assertThat(cached.getId()).isEqualTo(42L);
        assertThat(cached.getTimestamp()).isEqualTo(LocalDateTime.of(2026, 10, 3, 12, 0));
    }

    @Test
    @DisplayName("consume: invalid JSON goes to the DLQ unchanged, and nothing else happens")
    void consume_sendsInvalidJsonToDlq() {
        consumer.consume("{not json");
        verify(producer).publishToDlq("{not json");
        verifyNoInteractions(service, redisTemplate, messagingTemplate);
    }
    @Test
    @DisplayName("consume: a database failure goes to the DLQ; nothing is cached or broadcast")
    void consume_sendsToDlq_whenSaveFails() {
        when(service.saveLocation(anyLong(), anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("database down"));
        consumer.consume(PAYLOAD);
        verify(producer).publishToDlq(PAYLOAD);
        verifyNoInteractions(redisTemplate, messagingTemplate);
    }

    @Test
    @DisplayName("consume: broadcasts the raw payload to /topic/driver-locations")
    void consume_broadcastsRawPayload() {
        saveSucceeds();
        consumer.consume(PAYLOAD);
        verify(messagingTemplate).convertAndSend(WS_TOPIC, PAYLOAD);
    }


    @Test
    @DisplayName("consume: a missing latitude goes to the DLQ before anything is saved")
    void consume_sendsMissingLatitudeToDlq() {
        String noLatitude = "{\"driverId\":7,\"longitude\":3}";
        consumer.consume(noLatitude);
        verify(producer).publishToDlq(noLatitude);
        verifyNoInteractions(service, redisTemplate, messagingTemplate);
    }


}