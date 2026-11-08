package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.dto.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.Objects;

@Component
public class SensorEventMapper {

    public SensorEventAvro map(SensorEvent event) {
        Objects.requireNonNull(event, "Sensor event cannot be null");

        Object payload = switch (event) {
            case ClimateSensorEvent climate -> mapClimate(climate);
            case LightSensorEvent light -> mapLight(light);
            case MotionSensorEvent motion -> mapMotion(motion);
            case SwitchSensorEvent sw -> mapSwitch(sw);
            case TemperatureSensorEvent temp -> mapTemperature(temp);
            default -> throw new IllegalArgumentException(
                    "Unsupported sensor event: " + event.getClass().getName()
            );
        };

        return SensorEventAvro.newBuilder()
                .setId(Objects.requireNonNull(event.getId()))
                .setHubId(Objects.requireNonNull(event.getHubId()))
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    private ClimateSensorAvro mapClimate(ClimateSensorEvent event) {
        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(Objects.requireNonNull(event.getTemperatureC()))
                .setHumidity(Objects.requireNonNull(event.getHumidity()))
                .setCo2Level(Objects.requireNonNull(event.getCo2Level()))
                .build();
    }

    private LightSensorAvro mapLight(LightSensorEvent event) {
        return LightSensorAvro.newBuilder()
                .setLinkQuality(Objects.requireNonNull(event.getLinkQuality()))
                .setLuminosity(Objects.requireNonNull(event.getLuminosity()))
                .build();
    }

    private MotionSensorAvro mapMotion(MotionSensorEvent event) {
        return MotionSensorAvro.newBuilder()
                .setLinkQuality(Objects.requireNonNull(event.getLinkQuality()))
                .setMotion(Objects.requireNonNull(event.getMotion()))
                .setVoltage(Objects.requireNonNull(event.getVoltage()))
                .build();
    }

    private SwitchSensorAvro mapSwitch(SwitchSensorEvent event) {
        return SwitchSensorAvro.newBuilder()
                .setState(Objects.requireNonNull(event.getState()))
                .build();
    }

    private TemperatureSensorAvro mapTemperature(TemperatureSensorEvent event) {
        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(Objects.requireNonNull(event.getTemperatureC()))
                .setTemperatureF(Objects.requireNonNull(event.getTemperatureF()))
                .build();
    }
}
