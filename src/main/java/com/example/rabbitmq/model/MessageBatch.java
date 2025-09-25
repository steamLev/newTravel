package com.example.rabbitmq.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageBatch {
    private List<Message> messages;
    private String batchId;
    private int totalCount;
    private String routingKey;
    
    public MessageBatch(List<Message> messages, String routingKey) {
        this.messages = messages;
        this.batchId = java.util.UUID.randomUUID().toString();
        this.totalCount = messages != null ? messages.size() : 0;
        this.routingKey = routingKey;
    }
}