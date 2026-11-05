package ru.yandex.practicum.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.yandex.practicum.collector.dto.sensor.*;
import ru.yandex.practicum.collector.dto.hub.*;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class SensorEventJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @ParameterizedTest
    @MethodSource("sensorEvents")
    void shouldDeserializeSensorEvent(String json, Class<? extends SensorEvent> expectedClass) throws Exception {
        SensorEvent event = objectMapper.readValue(
                json, SensorEvent.class
        );

        assertInstanceOf(expectedClass, event);
        assertEquals("sensor-1", event.getId());
        assertEquals("hub-1", event.getHubId());
        assertNotNull(event.getTimestamp());
    }

    static Stream<Arguments> sensorEvents() {
        return Stream.of(
                Arguments.of("""
                        {
                            "id": "sensor-1",
                            "hubId": "hub-1",
                            "type": "LIGHT_SENSOR_EVENT",
                            "linkQuality": 75,
                            "luminosity": 59
                        }
                        """, LightSensorEvent.class),

                Arguments.of("""
                        {
                            "id": "sensor-1",
                            "hubId": "hub-1",
                            "type": "MOTION_SENSOR_EVENT",
                            "linkQuality": 90,
                            "motion": true,
                            "voltage": 220
                        }
                        """, MotionSensorEvent.class),

                Arguments.of("""
                        {
                            "id": "sensor-1",
                            "hubId": "hub-1",
                            "type": "TEMPERATURE_SENSOR_EVENT",
                            "temperatureC": 25,
                            "temperatureF": 77
                        }
                        """, TemperatureSensorEvent.class),

                Arguments.of("""
                        {
                            "id": "sensor-1",
                            "hubId": "hub-1",
                            "type": "CLIMATE_SENSOR_EVENT",
                            "temperatureC": 24,
                            "humidity": 50,
                            "co2Level": 400
                        }
                        """, ClimateSensorEvent.class),

                Arguments.of("""
                        {
                            "id": "sensor-1",
                            "hubId": "hub-1",
                            "type": "SWITCH_SENSOR_EVENT",
                            "state": true
                        }
                        """, SwitchSensorEvent.class)
        );
    }

    @ParameterizedTest
    @MethodSource("hubEvents")
    void shouldDeserializeHubEvent(String json, Class<? extends HubEvent> expectedClass) throws Exception {
        HubEvent event = objectMapper.readValue(
                json, HubEvent.class
        );

        assertInstanceOf(expectedClass, event);
        assertEquals("hub-1", event.getHubId());
        assertNotNull(event.getTimestamp());
    }

    static Stream<Arguments> hubEvents() {
        return Stream.of(
                Arguments.of("""
                        {
                            "hubId": "hub-1",
                            "type": "DEVICE_ADDED",
                            "id": "sensor-1",
                            "deviceType": "MOTION_SENSOR"
                        }
                        """, DeviceAddedEvent.class),

                Arguments.of("""
                        {
                            "hubId": "hub-1",
                            "type": "DEVICE_REMOVED",
                            "id": "sensor-1"
                        }
                        """, DeviceRemovedEvent.class),

                Arguments.of("""
                        {
                            "hubId": "hub-1",
                            "type": "SCENARIO_ADDED",
                            "name": "Evening lights",
                            "conditions": [
                                {
                                    "sensorId": "sensor-1",
                                    "type": "LUMINOSITY",
                                    "operation": "LOWER_THAN",
                                    "value": 50
                                }
                            ],
                            "actions": [
                                {
                                    "sensorId": "sensor-2",
                                    "type": "ACTIVATE"
                                }
                            ]
                        }
                        """, ScenarioAddedEvent.class),

                Arguments.of("""
                        {
                            "hubId": "hub-1",
                            "type": "SCENARIO_REMOVED",
                            "name": "Evening lights"
                        }
                        """, ScenarioRemovedEvent.class)
        );
    }

    @Test
    void shouldRejectUnknownSensorType() {
        String json = """
                {
                    "id": "sensor-1",
                    "hubId": "hub-1",
                    "type": "UNKNOWN_EVENT"
                }
                """;

        assertThrows(
                InvalidTypeIdException.class,
                () -> objectMapper.readValue(json, SensorEvent.class)
        );
    }

    @Test
    void shouldRejectUnknownHubType() {
        String json = """
                {
                    "hubId": "hub-1",
                    "type": "UNKNOWN_EVENT"
                }
                """;

        assertThrows(
                InvalidTypeIdException.class,
                () -> objectMapper.readValue(json, HubEvent.class)
        );
    }

    @Test
    void shouldRejectMissingRequiredField() throws Exception {
        String json = """
                {
                    "id": "sensor-1",
                    "hubId": "hub-1",
                    "type": "MOTION_SENSOR_EVENT",
                    "linkQuality": 75,
                    "voltage": 220
                }
                """;

        SensorEvent event = objectMapper.readValue(
                json, SensorEvent.class
        );

        Set<ConstraintViolation<SensorEvent>> violations =
                validator.validate(event);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getPropertyPath()
                                .toString().equals("motion"))
        );
    }

    @Test
    void shouldRejectBlankHubId() throws Exception {
        String json = """
                {
                    "id": "sensor-1",
                    "hubId": "",
                    "type": "LIGHT_SENSOR_EVENT"
                }
                """;

        SensorEvent event = objectMapper.readValue(
                json, SensorEvent.class
        );

        Set<ConstraintViolation<SensorEvent>> violations =
                validator.validate(event);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getPropertyPath()
                                .toString().equals("hubId"))
        );
    }

    @Test
    void shouldDeserializeScenarioWithNestedObjects() throws Exception {
        String json = """
                {
                    "hubId": "hub-1",
                    "type": "SCENARIO_ADDED",
                    "name": "Evening lights",
                    "conditions": [
                        {
                            "sensorId": "sensor-1",
                            "type": "LUMINOSITY",
                            "operation": "LOWER_THAN",
                            "value": 50
                        }
                    ],
                    "actions": [
                        {
                            "sensorId": "sensor-2",
                            "type": "ACTIVATE"
                        }
                    ]
                }
                """;

        HubEvent event = objectMapper.readValue(
                json, HubEvent.class
        );

        ScenarioAddedEvent scenario =
                assertInstanceOf(ScenarioAddedEvent.class, event);

        assertEquals("Evening lights", scenario.getName());

        assertEquals(1, scenario.getConditions().size());
        assertEquals(1, scenario.getActions().size());

        ScenarioCondition condition =
                scenario.getConditions().get(0);

        assertEquals("sensor-1", condition.getSensorId());
        assertEquals(ConditionType.LUMINOSITY, condition.getType());
        assertEquals(
                ConditionOperation.LOWER_THAN,
                condition.getOperation()
        );
        assertEquals(50, condition.getValue());

        DeviceAction action = scenario.getActions().get(0);

        assertEquals("sensor-2", action.getSensorId());
        assertEquals(DeviceActionType.ACTIVATE, action.getType());
    }

    @Test
    void shouldRejectEmptyScenarioConditions() throws Exception {
        String json = """
                {
                    "hubId": "hub-1",
                    "type": "SCENARIO_ADDED",
                    "name": "Evening lights",
                    "conditions": [],
                    "actions": [
                        {
                            "sensorId": "sensor-2",
                            "type": "ACTIVATE"
                        }
                    ]
                }
                """;

        HubEvent event = objectMapper.readValue(
                json, HubEvent.class
        );

        Set<ConstraintViolation<HubEvent>> violations =
                validator.validate(event);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("conditions"))
        );
    }

    @Test
    void shouldDeserializeTimestamp() throws Exception {
        String json = """
                {
                    "id": "sensor-1",
                    "hubId": "hub-1",
                    "type": "LIGHT_SENSOR_EVENT",
                    "timestamp": "2024-08-06T15:11:24.157Z"
                }
                """;

        SensorEvent event = objectMapper.readValue(
                json, SensorEvent.class
        );

        assertEquals(
                Instant.parse("2024-08-06T15:11:24.157Z"),
                event.getTimestamp()
        );
    }
}
