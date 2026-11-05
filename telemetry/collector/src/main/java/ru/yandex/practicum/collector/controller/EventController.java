package ru.yandex.practicum.collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.collector.dto.hub.HubEvent;
import ru.yandex.practicum.collector.dto.sensor.SensorEvent;
import ru.yandex.practicum.collector.kafka.KafkaEventPublisher;
import ru.yandex.practicum.collector.mapper.HubEventMapper;
import ru.yandex.practicum.collector.mapper.SensorEventMapper;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final SensorEventMapper sensorEventMapper;
    private final HubEventMapper hubEventMapper;
    private final KafkaEventPublisher kafkaEventPublisher;

    @PostMapping("/sensors")
    public void collectSensorEvent(
            @Valid @RequestBody SensorEvent event
    ) {
        kafkaEventPublisher.sendSensorEvent(sensorEventMapper.map(event));
    }

    @PostMapping("/hubs")
    public void collectHubEvent(
            @Valid @RequestBody HubEvent event
    ) {
        kafkaEventPublisher.sendHubEvent(
                hubEventMapper.map(event)
        );
    }
}
