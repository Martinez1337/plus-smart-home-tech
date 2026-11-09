package ru.yandex.practicum.collector.handlers.sensor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.kafka.KafkaEventPublisher;
import ru.yandex.practicum.collector.mapper.SensorProtoMapper;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Component
@RequiredArgsConstructor
public class ClimateSensorEventHandler implements SensorEventHandler {

    private final SensorProtoMapper mapper;
    private final KafkaEventPublisher publisher;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        publisher.sendSensorEvent(mapper.map(event));
    }
}
