package ru.yandex.practicum.collector.handlers.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.kafka.KafkaEventPublisher;
import ru.yandex.practicum.collector.mapper.HubProtoMapper;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {

    private final HubProtoMapper mapper;
    private final KafkaEventPublisher publisher;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        publisher.sendHubEvent(mapper.map(event));
    }
}
