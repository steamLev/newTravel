package com.example.rabbitmq.repository;

import com.example.rabbitmq.model.DeliveryStatus;
import com.example.rabbitmq.model.OutboundMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface OutboundMessageRepository extends JpaRepository<OutboundMessageEntity, String> {

    @Query("select m from OutboundMessageEntity m " +
            "where m.status in :statuses and m.attemptCount < m.maxAttempts")
    List<OutboundMessageEntity> findActiveMessages(Collection<DeliveryStatus> statuses);
}
