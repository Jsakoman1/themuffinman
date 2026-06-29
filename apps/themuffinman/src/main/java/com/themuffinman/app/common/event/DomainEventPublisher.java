package com.themuffinman.app.common.event;

public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
