package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.dto.hub.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.Objects;

@Component
public class HubEventMapper {

    public HubEventAvro map(HubEvent event) {
        Objects.requireNonNull(event, "Hub event cannot be null");

        Object payload = switch (event) {
            case DeviceAddedEvent added -> mapDeviceAdded(added);
            case DeviceRemovedEvent removed -> mapDeviceRemoved(removed);
            case ScenarioAddedEvent added -> mapScenarioAdded(added);
            case ScenarioRemovedEvent removed -> mapScenarioRemoved(removed);
            default -> throw new IllegalArgumentException(
                    "Unsupported hub event: " + event.getClass().getName()
            );
        };

        return HubEventAvro.newBuilder()
                .setHubId(Objects.requireNonNull(event.getHubId()))
                .setTimestamp(Objects.requireNonNull(event.getTimestamp()))
                .setPayload(payload)
                .build();
    }

    private DeviceAddedEventAvro mapDeviceAdded(DeviceAddedEvent event) {
        DeviceType deviceType = Objects.requireNonNull(event.getDeviceType());

        return DeviceAddedEventAvro.newBuilder()
                .setId(Objects.requireNonNull(event.getId()))
                .setType(DeviceTypeAvro.valueOf(deviceType.name()))
                .build();
    }

    private DeviceRemovedEventAvro mapDeviceRemoved(DeviceRemovedEvent event) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(Objects.requireNonNull(event.getId()))
                .build();
    }

    private ScenarioAddedEventAvro mapScenarioAdded(ScenarioAddedEvent event) {
        var conditions = Objects.requireNonNull(event.getConditions());
        var actions = Objects.requireNonNull(event.getActions());

        return ScenarioAddedEventAvro.newBuilder()
                .setName(Objects.requireNonNull(event.getName()))
                .setConditions(conditions.stream()
                        .map(this::mapCondition)
                        .toList())
                .setActions(actions.stream()
                        .map(this::mapAction)
                        .toList())
                .build();
    }

    private ScenarioRemovedEventAvro mapScenarioRemoved(ScenarioRemovedEvent event) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(Objects.requireNonNull(event.getName()))
                .build();
    }

    private ScenarioConditionAvro mapCondition(ScenarioCondition condition) {
        Objects.requireNonNull(condition);

        ConditionType type = Objects.requireNonNull(condition.getType());
        ConditionOperation operation = Objects.requireNonNull(condition.getOperation());

        return ScenarioConditionAvro.newBuilder()
                .setSensorId(Objects.requireNonNull(condition.getSensorId()))
                .setType(ConditionTypeAvro.valueOf(type.name()))
                .setOperation(ConditionOperationAvro.valueOf(operation.name()))
                .setValue(condition.getValue())
                .build();
    }

    private DeviceActionAvro mapAction(DeviceAction action) {
        Objects.requireNonNull(action);

        DeviceActionType type = Objects.requireNonNull(action.getType());

        return DeviceActionAvro.newBuilder()
                .setSensorId(Objects.requireNonNull(action.getSensorId()))
                .setType(ActionTypeAvro.valueOf(type.name()))
                .setValue(action.getValue())
                .build();
    }
}
