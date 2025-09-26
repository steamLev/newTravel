package com.example.rabbitmq.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String id;
    private String content;
    private String type;
    private String priority;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    public Message(String content, String type, String priority) {
        this.id = UUID.randomUUID().toString();
        this.content = content;
        this.type = type;
        this.priority = priority;
        this.timestamp = LocalDateTime.now();
    }
}