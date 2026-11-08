package ru.yandex.practicum.collector.mapper;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.collector.dto.hub.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HubEventMapperTest {

    private final HubEventMapper mapper = new HubEventMapper();

    private static final Instant TIMESTAMP = Instant.parse("2024-08-06T15:11:24Z");

    @Test
    void shouldMapDeviceAddedEvent() {
        DeviceAddedEvent event = new DeviceAddedEvent();
        event.setHubId("hub-1");
        event.setTimestamp(TIMESTAMP);
        event.setId("sensor-1");
        event.setDeviceType(DeviceType.MOTION_SENSOR);

        HubEventAvro result = mapper.map(event);
        assertCommonFields(event, result);

        DeviceAddedEventAvro payload = assertInstanceOf(
                DeviceAddedEventAvro.class, result.getPayload()
        );

        assertEquals("sensor-1", payload.getId().toString());
        assertEquals(DeviceTypeAvro.MOTION_SENSOR, payload.getType());
    }

    @Test
    void shouldMapDeviceRemovedEvent() {
        DeviceRemovedEvent event = new DeviceRemovedEvent();
        event.setHubId("hub-1");
        event.setTimestamp(TIMESTAMP);
        event.setId("sensor-2");

        HubEventAvro result = mapper.map(event);
        assertCommonFields(event, result);

        DeviceRemovedEventAvro payload = assertInstanceOf(
                DeviceRemovedEventAvro.class, result.getPayload()
        );

        assertEquals("sensor-2", payload.getId().toString());
    }

    @Test
    void shouldMapScenarioAddedEvent() {
        ScenarioCondition condition = new ScenarioCondition();
        condition.setSensorId("sensor-light");
        condition.setType(ConditionType.LUMINOSITY);
        condition.setOperation(ConditionOperation.LOWER_THAN);
        condition.setValue(50);

        DeviceAction action = new DeviceAction();
        action.setSensorId("sensor-switch");
        action.setType(DeviceActionType.SET_VALUE);
        action.setValue(1);

        ScenarioAddedEvent event = new ScenarioAddedEvent();
        event.setHubId("hub-1");
        event.setTimestamp(TIMESTAMP);
        event.setName("Evening lights");
        event.setConditions(List.of(condition));
        event.setActions(List.of(action));

        HubEventAvro result = mapper.map(event);
        assertCommonFields(event, result);

        ScenarioAddedEventAvro payload = assertInstanceOf(
                ScenarioAddedEventAvro.class, result.getPayload()
        );

        assertEquals("Evening lights", payload.getName().toString());
        assertEquals(1, payload.getConditions().size());
        assertEquals(1, payload.getActions().size());

        ScenarioConditionAvro mappedCondition =
                payload.getConditions().get(0);

        assertAll(
                () -> assertEquals("sensor-light",
                        mappedCondition.getSensorId().toString()),
                () -> assertEquals(ConditionTypeAvro.LUMINOSITY,
                        mappedCondition.getType()),
                () -> assertEquals(ConditionOperationAvro.LOWER_THAN,
                        mappedCondition.getOperation()),
                () -> assertEquals(50, mappedCondition.getValue())
        );

        DeviceActionAvro mappedAction =
                payload.getActions().get(0);

        assertAll(
                () -> assertEquals("sensor-switch",
                        mappedAction.getSensorId().toString()),
                () -> assertEquals(ActionTypeAvro.SET_VALUE,
                        mappedAction.getType()),
                () -> assertEquals(1, mappedAction.getValue())
        );
    }

    @Test
    void shouldMapScenarioRemovedEvent() {
        ScenarioRemovedEvent event = new ScenarioRemovedEvent();
        event.setHubId("hub-1");
        event.setTimestamp(TIMESTAMP);
        event.setName("Evening lights");

        HubEventAvro result = mapper.map(event);
        assertCommonFields(event, result);

        ScenarioRemovedEventAvro payload = assertInstanceOf(
                ScenarioRemovedEventAvro.class, result.getPayload()
        );

        assertEquals("Evening lights", payload.getName().toString());
    }

    @Test
    void shouldMapNullableValues() {
        ScenarioCondition condition = new ScenarioCondition();
        condition.setSensorId("sensor-1");
        condition.setType(ConditionType.MOTION);
        condition.setOperation(ConditionOperation.EQUALS);
        condition.setValue(null);

        DeviceAction action = new DeviceAction();
        action.setSensorId("sensor-2");
        action.setType(DeviceActionType.ACTIVATE);
        action.setValue(null);

        ScenarioAddedEvent event = new ScenarioAddedEvent();
        event.setHubId("hub-1");
        event.setTimestamp(TIMESTAMP);
        event.setName("Motion lights");
        event.setConditions(List.of(condition));
        event.setActions(List.of(action));

        HubEventAvro result = mapper.map(event);

        ScenarioAddedEventAvro payload = assertInstanceOf(
                ScenarioAddedEventAvro.class, result.getPayload()
        );

        assertNull(payload.getConditions().get(0).getValue());
        assertNull(payload.getActions().get(0).getValue());
    }

    @Test
    void shouldRejectNullEvent() {
        assertThrows(
                NullPointerException.class,
                () -> mapper.map(null)
        );
    }

    private void assertCommonFields(
            HubEvent source,
            HubEventAvro result
    ) {
        assertAll(
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
