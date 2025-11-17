package com.example.rabbitmq.model;

public enum DeliveryStatus {
    PENDING,
    IN_PROGRESS,
    SENT,
    FAILED;

    public boolean isTerminal() {
        return this == SENT || this == FAILED;
    }
}
