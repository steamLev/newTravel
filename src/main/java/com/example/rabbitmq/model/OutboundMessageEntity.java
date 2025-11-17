package com.example.rabbitmq.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "outbound_messages")
public class OutboundMessageEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 2048)
    private String content;

    @Column(length = 64)
    private String type;

    @Column(length = 16)
    private String priority;

    @Column(name = "routing_key", length = 128, nullable = false)
    private String routingKey;

    @Column(name = "attempt_count")
    private int attemptCount;

    @Column(name = "max_attempts")
    private int maxAttempts;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private DeliveryStatus status;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "last_error", length = 1024)
    private String lastError;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
