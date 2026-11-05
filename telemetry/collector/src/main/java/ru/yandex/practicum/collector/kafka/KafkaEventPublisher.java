package ru.yandex.practicum.collector.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.util.Objects;

@Service
public class KafkaEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final Producer<String, SpecificRecordBase> producer;
    private final String sensorsTopic;
    private final String hubsTopic;

    public KafkaEventPublisher(
            Producer<String, SpecificRecordBase> producer,
            @Value("${collector.kafka.topics.sensors}") String sensorsTopic,
            @Value("${collector.kafka.topics.hubs}") String hubsTopic
    ) {
        this.producer = producer;
        this.sensorsTopic = sensorsTopic;
        this.hubsTopic = hubsTopic;
    }

    public void sendSensorEvent(SensorEventAvro event) {
        Objects.requireNonNull(event, "Sensor event cannot be null");

        send(sensorsTopic, event.getHubId(), event);
    }

    public void sendHubEvent(HubEventAvro event) {
        Objects.requireNonNull(event, "Hub event cannot be null");

        send(hubsTopic, event.getHubId(), event);
    }

    private void send(String topic, String key, SpecificRecordBase event) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, key, event);

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send event to topic {}, key={}", topic, key, exception);
                return;
            }

            log.debug(
                    "Event sent: topic={}, partition={}, offset={}",
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset()
            );
        });
    }
}
