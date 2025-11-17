package com.example.rabbitmq.repository;

import com.example.rabbitmq.entity.PendingMessageEntity;
import com.example.rabbitmq.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PendingMessageRepository extends JpaRepository<PendingMessageEntity, Long> {

    Optional<PendingMessageEntity> findByMessageId(String messageId);

    List<PendingMessageEntity> findByStatusIn(Collection<DeliveryStatus> statuses);
}
