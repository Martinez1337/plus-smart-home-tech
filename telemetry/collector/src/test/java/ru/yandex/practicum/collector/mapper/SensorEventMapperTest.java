package ru.yandex.practicum.collector.mapper;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.collector.dto.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SensorEventMapperTest {

    private final SensorEventMapper mapper = new SensorEventMapper();

    private static final Instant TIMESTAMP = Instant.parse("2024-08-06T15:11:24Z");

    @Test
    void shouldMapMotionSensorEvent() {
        MotionSensorEvent event = new MotionSensorEvent();
        event.setId("motion-1");
        event.setHubId("hub-1");
        event.setTimestamp(TIMESTAMP);
        event.setLinkQuality(75);
        event.setMotion(true);
        event.setVoltage(220);

        SensorEventAvro result = mapper.map(event);

        assertCommonFields(event, result);

        MotionSensorAvro payload = assertInstanceOf(MotionSensorAvro.class, result.getPayload());

        assertAll(
                () -> assertEquals(75, payload.getLinkQuality()),
                () -> assertTrue(payload.getMotion()),
                () -> assertEquals(220, payload.getVoltage())
        );
    }

    @Test
    void shouldMapLightSensorEvent() {
        LightSensorEvent event = new LightSensorEvent();
        event.setId("light-1");
        event.setHubId("hub-1");
        event.setTimestamp(TIMESTAMP);
        event.setLinkQuality(90);
        event.setLuminosity(350);

        SensorEventAvro result = mapper.map(event);

        assertCommonFields(event, result);

        LightSensorAvro payload = assertInstanceOf(LightSensorAvro.class, result.getPayload());

        assertAll(
                () -> assertEquals(90, payload.getLinkQuality()),
                () -> assertEquals(350, payload.getLuminosity())
        );
    }

    @Test
    void shouldMapClimateSensorEvent() {
        ClimateSensorEvent event = new ClimateSensorEvent();
        event.setId("climate-1");
        event.setHubId("hub-2");
        event.setTimestamp(TIMESTAMP);
        event.setTemperatureC(24);
        event.setHumidity(65);
        event.setCo2Level(800);

        SensorEventAvro result = mapper.map(event);

        assertCommonFields(event, result);

        ClimateSensorAvro payload = assertInstanceOf(ClimateSensorAvro.class, result.getPayload());

        assertAll(
                () -> assertEquals(24, payload.getTemperatureC()),
                () -> assertEquals(65, payload.getHumidity()),
                () -> assertEquals(800, payload.getCo2Level())
        );
    }

    @Test
    void shouldMapTemperatureSensorEvent() {
        TemperatureSensorEvent event = new TemperatureSensorEvent();
        event.setId("temperature-1");
        event.setHubId("hub-2");
        event.setTimestamp(TIMESTAMP);
        event.setTemperatureC(-10);
        event.setTemperatureF(14);

        SensorEventAvro result = mapper.map(event);

        assertCommonFields(event, result);

        TemperatureSensorAvro payload = assertInstanceOf(TemperatureSensorAvro.class, result.getPayload());

        assertAll(
                () -> assertEquals(-10, payload.getTemperatureC()),
                () -> assertEquals(14, payload.getTemperatureF())
        );
    }

    @Test
    void shouldMapSwitchSensorEvent() {
        SwitchSensorEvent event = new SwitchSensorEvent();
        event.setId("switch-1");
        event.setHubId("hub-3");
        event.setTimestamp(TIMESTAMP);
        event.setState(false);

        SensorEventAvro result = mapper.map(event);

        assertCommonFields(event, result);

        SwitchSensorAvro payload = assertInstanceOf(SwitchSensorAvro.class, result.getPayload());

        assertFalse(payload.getState());
    }

    @Test
    void shouldRejectNullEvent() {
        assertThrows(
                NullPointerException.class,
                () -> mapper.map(null)
        );
    }

    @Test
    void shouldRejectNullRequiredField() {
        MotionSensorEvent event = new MotionSensorEvent();
        event.setId("motion-1");
        event.setHubId("hub-1");
        event.setTimestamp(TIMESTAMP);
        event.setLinkQuality(75);
        event.setMotion(null);
        event.setVoltage(220);

        assertThrows(
                NullPointerException.class,
                () -> mapper.map(event)
        );
    }

    @Test
    void shouldRejectUnsupportedSensorEvent() {
        SensorEvent event = new SensorEvent() {
            @Override
            public SensorEventType getType() {
                return SensorEventType.LIGHT_SENSOR_EVENT;
            }
        };

        event.setId("unknown-1");
        event.setHubId("hub-1");
        event.setTimestamp(TIMESTAMP);

        assertThrows(IllegalArgumentException.class, () -> mapper.map(event));
    }

    private void assertCommonFields(
            SensorEvent source,
            SensorEventAvro result
    ) {
        assertAll(
                () -> assertEquals(
                        source.getId(),
                        result.getId().toString()
                ),
                () -> assertEquals(
                        source.getHubId(),
                        result.getHubId().toString()
                ),
                () -> assertEquals(
                        source.getTimestamp(),
                        result.getTimestamp()
                ),
                () -> assertNotNull(result.getPayload())
        );
    }
}
