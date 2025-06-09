package com.bookworm.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public abstract class DomainEvent {
    private final LocalDateTime occurredAt;

    protected DomainEvent(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}