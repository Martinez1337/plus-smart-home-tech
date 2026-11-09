
package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.Objects;

@Component
public class HubProtoMapper {

    public HubEventAvro map(HubEventProto event) {
        Objects.requireNonNull(event, "Hub event cannot be null");

        if (!event.hasTimestamp()) {
            throw new IllegalArgumentException("Timestamp is required");
        }

        Object payload = switch (event.getPayloadCase()) {
            case DEVICE_ADDED -> mapDeviceAdded(event);
            case DEVICE_REMOVED -> mapDeviceRemoved(event);
            case SCENARIO_ADDED -> mapScenarioAdded(event);
            case SCENARIO_REMOVED -> mapScenarioRemoved(event);
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Hub event payload is not set");
        };

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()
                ))
                .setPayload(payload)
                .build();
    }

    private DeviceAddedEventAvro mapDeviceAdded(HubEventProto event) {
        var device = event.getDeviceAdded();

        return DeviceAddedEventAvro.newBuilder()
                .setId(device.getId())
                .setType(DeviceTypeAvro.valueOf(
                        device.getType().name()
                ))
                .build();
    }

    private DeviceRemovedEventAvro mapDeviceRemoved(HubEventProto event) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(event.getDeviceRemoved().getId())
                .build();
    }

    private ScenarioAddedEventAvro mapScenarioAdded(HubEventProto event) {
        var scenario = event.getScenarioAdded();

        var conditions = scenario.getConditionList().stream()
                .map(this::mapCondition)
                .toList();

        var actions = scenario.getActionList().stream()
                .map(this::mapAction)
                .toList();

        return ScenarioAddedEventAvro.newBuilder()
                .setName(scenario.getName())
                .setConditions(conditions)
                .setActions(actions)
                .build();
    }

    private ScenarioConditionAvro mapCondition(ScenarioConditionProto condition) {
        Object value = switch (condition.getValueCase()) {
            case BOOL_VALUE -> condition.getBoolValue();
            case INT_VALUE -> condition.getIntValue();
            case VALUE_NOT_SET -> null;
        };

        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(
                        condition.getType().name()
                ))
                .setOperation(ConditionOperationAvro.valueOf(
                        condition.getOperation().name()
                ))
                .setValue(value)
                .build();
    }

    private DeviceActionAvro mapAction(DeviceActionProto action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(
                        action.getType().name()
                ))
                .setValue(
                        action.hasValue() ? action.getValue() : null
                )
                .build();
    }

    private ScenarioRemovedEventAvro mapScenarioRemoved(HubEventProto event) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(event.getScenarioRemoved().getName())
                .build();
    }
}
