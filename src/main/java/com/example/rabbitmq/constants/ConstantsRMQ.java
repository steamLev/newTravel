package com.example.rabbitmq.constants;

/**
 * Константы для RabbitMQ
 */
public class ConstantsRMQ {
    
    // Очереди
    public static final String MESSAGE_QUEUE = "message.queue";
    public static final String BINARY_QUEUE = "binary.queue";
    
    // Exchange
    public static final String MESSAGE_EXCHANGE = "message.exchange";
    public static final String BINARY_EXCHANGE = "binary.exchange";
    
    // Routing Keys
    public static final String MESSAGE_ROUTING_KEY = "message.routing.key";
    public static final String BINARY_ROUTING_KEY = "binary.routing.key";
    
    // Другие константы
    public static final String CONVERSION_QUEUE = "conversion.queue";
    public static final String PDF_CONVERSION_QUEUE = "pdf.conversion.queue";
}