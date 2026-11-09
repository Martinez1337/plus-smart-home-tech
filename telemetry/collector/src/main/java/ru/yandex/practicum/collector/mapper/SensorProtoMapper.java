package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.Objects;

@Component
public class SensorProtoMapper {

    public SensorEventAvro map(SensorEventProto event) {
        Objects.requireNonNull(event, "Sensor event cannot be null");

        if (!event.hasTimestamp()) {
            throw new IllegalArgumentException("Timestamp is required");
        }

        Object payload = switch (event.getPayloadCase()) {
            case MOTION_SENSOR -> mapMotion(event);
            case LIGHT_SENSOR -> mapLight(event);
            case CLIMATE_SENSOR -> mapClimate(event);
            case TEMPERATURE_SENSOR -> mapTemperature(event);
            case SWITCH_SENSOR -> mapSwitch(event);
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException(
                    "Sensor event payload is not set"
            );
        };

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()
                ))
                .setPayload(payload)
                .build();
    }

    private MotionSensorAvro mapMotion(SensorEventProto event) {
        var sensor = event.getMotionSensor();

        return MotionSensorAvro.newBuilder()
                .setLinkQuality(sensor.getLinkQuality())
                .setMotion(sensor.getMotion())
                .setVoltage(sensor.getVoltage())
                .build();
    }

    private LightSensorAvro mapLight(SensorEventProto event) {
        var sensor = event.getLightSensor();

        return LightSensorAvro.newBuilder()
                .setLinkQuality(sensor.getLinkQuality())
                .setLuminosity(sensor.getLuminosity())
                .build();
    }

    private ClimateSensorAvro mapClimate(SensorEventProto event) {
        var sensor = event.getClimateSensor();

        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(sensor.getTemperatureC())
                .setHumidity(sensor.getHumidity())
                .setCo2Level(sensor.getCo2Level())
                .build();
    }

    private TemperatureSensorAvro mapTemperature(SensorEventProto event) {
        var sensor = event.getTemperatureSensor();

        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(sensor.getTemperatureC())
                .setTemperatureF(sensor.getTemperatureF())
                .build();
    }

    private SwitchSensorAvro mapSwitch(SensorEventProto event) {
        int state = event.getSwitchSensor().getState();

        if (state != 0 && state != 1) {
            throw new IllegalArgumentException(
                    "Invalid switch state: " + state
            );
        }

        return SwitchSensorAvro.newBuilder().setState(state == 1).build();
    }
}
