package com.example.rabbitmq.controller;

import com.example.rabbitmq.model.ChannelStatus;
import com.example.rabbitmq.model.Message;
import com.example.rabbitmq.model.MessageBatch;
import com.example.rabbitmq.service.ChannelMonitorService;
import com.example.rabbitmq.service.MessageSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageSenderService messageSenderService;
    private final ChannelMonitorService channelMonitorService;

    /**
     * Отправляет список сообщений
     */
    @PostMapping("/send")
    public CompletableFuture<ResponseEntity<String>> sendMessages(
            @RequestBody List<Message> messages,
            @RequestParam(defaultValue = "message.routing.key") String routingKey) {
        
        log.info("Received request to send {} messages", messages.size());
        
        MessageBatch batch = new MessageBatch(messages, routingKey);
        
        return messageSenderService.sendMessageBatch(batch)
                .thenApply(result -> {
                    log.info("Batch {} зарегистрирован на отложенную доставку ({} сообщений)",
                            result.getBatchId(), result.getTotalCount());
                    return ResponseEntity.ok("Сообщения поставлены в очередь на доставку через 60 секунд. Batch ID: " + result.getBatchId());
                })
                .exceptionally(throwable -> {
                    log.error("Failed to send messages", throwable);
                    return ResponseEntity.internalServerError()
                            .body("Failed to send messages: " + throwable.getMessage());
                });
    }

    /**
     * Отправляет одно сообщение
     */
    @PostMapping("/send/single")
    public CompletableFuture<ResponseEntity<String>> sendSingleMessage(
            @RequestBody Message message,
            @RequestParam(defaultValue = "message.routing.key") String routingKey) {
        
        log.info("Received request to send single message: {}", message.getId());
        
        return messageSenderService.sendSingleMessage(message, routingKey)
                .thenApply(v -> ResponseEntity.ok("Сообщение поставлено в очередь на доставку через 60 секунд: " + message.getId()))
                .exceptionally(throwable -> {
                    log.error("Failed to send message", throwable);
                    return ResponseEntity.internalServerError()
                            .body("Failed to send message: " + throwable.getMessage());
                });
    }

    /**
     * Получает статус каналов
     */
    @GetMapping("/channels/status")
    public ResponseEntity<ChannelStatus> getChannelStatus() {
        ChannelStatus status = channelMonitorService.getChannelStatus();
        log.debug("Channel status requested: {}", status);
        return ResponseEntity.ok(status);
    }

    /**
     * Проверяет доступность каналов
     */
    @GetMapping("/channels/available")
    public ResponseEntity<Boolean> areChannelsAvailable() {
        boolean available = messageSenderService.canSendMessages();
        log.debug("Channel availability check: {}", available);
        return ResponseEntity.ok(available);
    }

    /**
     * Создает тестовые сообщения
     */
    @PostMapping("/test/create")
    public ResponseEntity<List<Message>> createTestMessages(
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "test") String type) {
        
        List<Message> messages = java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new Message(
                        "Test message " + (i + 1),
                        type,
                        i % 3 == 0 ? "high" : i % 3 == 1 ? "medium" : "low"
                ))
                .toList();
        
        log.info("Created {} test messages of type {}", count, type);
        return ResponseEntity.ok(messages);
    }

    /**
     * Отправляет тестовые сообщения
     */
    @PostMapping("/test/send")
    public CompletableFuture<ResponseEntity<String>> sendTestMessages(
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "test") String type,
            @RequestParam(defaultValue = "message.routing.key") String routingKey) {
        
        List<Message> messages = java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new Message(
                        "Test message " + (i + 1),
                        type,
                        i % 3 == 0 ? "high" : i % 3 == 1 ? "medium" : "low"
                ))
                .toList();
        
        MessageBatch batch = new MessageBatch(messages, routingKey);
        
        return messageSenderService.sendMessageBatch(batch)
                .thenApply(result -> ResponseEntity.ok(
                        "Тестовые сообщения поставлены в очередь на отложенную доставку. Batch ID: " +
                                result.getBatchId() +
                                ", Count: " + result.getTotalCount()))
                .exceptionally(throwable -> ResponseEntity.internalServerError()
                        .body("Failed to send test messages: " + throwable.getMessage()));
    }
}