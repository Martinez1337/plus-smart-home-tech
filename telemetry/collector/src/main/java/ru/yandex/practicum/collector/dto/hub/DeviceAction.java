package ru.yandex.practicum.collector.dto.hub;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceAction {
    private String sensorId;
    private DeviceActionType type;
    private Integer value;
}
