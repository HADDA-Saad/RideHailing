package com.ridehailing.location.service;

import com.ridehailing.location.dto.DriverLocationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationProducerTest {
    @Mock
    KafkaTemplate<String,String> kafkaTemplate;
    @Captor
    ArgumentCaptor<String> payloadCaptor;
    ObjectMapper objectMapper= JsonMapper.builder().build();;

    private LocationProducer producer;

    @BeforeEach
    void setup(){
        producer=new LocationProducer(kafkaTemplate,objectMapper);
    }


    @Test
    @DisplayName("publish: the payload is the request serialized as JSON")
    void publish_payloadIsRequestAsJson() {
        producer.publish(new DriverLocationRequest(7L,1000L,1200L));
        verify(kafkaTemplate).send(eq("driver-locations"),eq("7"),payloadCaptor.capture());
        JsonNode json = objectMapper.readTree(payloadCaptor.getValue());

        assertThat(json.get("driverId").asLong()).isEqualTo(7L);
        assertThat(json.get("latitude").asLong()).isEqualTo(1000L);
        assertThat(json.get("longitude").asLong()).isEqualTo(1200L);

    }
    @Test
    @DisplayName("publish: the payload round-trips back into an equal request")
    void publish_payloadRoundTripsToEqualRequest() {
        DriverLocationRequest request=new DriverLocationRequest(7L,1000L,1200L);
        producer.publish(request);

        verify(kafkaTemplate).send(eq("driver-locations"),eq("7"),payloadCaptor.capture());
        assertThat(objectMapper.readValue(payloadCaptor.getValue(),DriverLocationRequest.class)).isEqualTo(request);


    }

    @Test
    @DisplayName("publish: a null driver id is wrapped in 'Failed to publishlocation event' and nothing is sent")
    void publish_wrapsNullDriverId_andSendsNothing() {
        DriverLocationRequest noDriver = new DriverLocationRequest(null, 36L, 3L);
        assertThatThrownBy(() -> producer.publish(noDriver))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to publish location event")
                .hasCauseInstanceOf(NullPointerException.class);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void publishToDlq() {
        producer.publishToDlq("{not json at all");

        verify(kafkaTemplate).send("driver-locations-dlq","{not json at all");
        verify(kafkaTemplate,never()).send(anyString(),anyString(),anyString());
    }
}