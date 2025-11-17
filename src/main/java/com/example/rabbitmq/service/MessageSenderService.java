package com.example.rabbitmq.service;

import com.example.rabbitmq.model.Message;
import com.example.rabbitmq.model.MessageBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSenderService {

    private final DelayedMessageDispatcher delayedMessageDispatcher;
    private final ChannelMonitorService channelMonitorService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * Отправляет список сообщений в RabbitMQ с проверкой доступности каналов
     */
    public CompletableFuture<MessageBatch> sendMessageBatch(MessageBatch messageBatch) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Batch {} поставлен в очередь на отложенную отправку ({} сообщений)",
                    messageBatch.getBatchId(), messageBatch.getTotalCount());

            delayedMessageDispatcher.enqueueBatch(messageBatch);
            return messageBatch;
        }, executorService);
    }

    /**
     * Регистрирует одно сообщение на отложенную отправку
     */
    public CompletableFuture<Void> sendSingleMessage(Message message, String routingKey) {
        return CompletableFuture.runAsync(() -> {
            delayedMessageDispatcher.enqueueSingle(
                    message,
                    routingKey != null ? routingKey : "message.routing.key"
            );
        }, executorService);
    }

    /**
     * Отправляет сообщения асинхронно с контролем каналов
     */
    public CompletableFuture<List<Message>> sendMessagesAsync(List<Message> messages, String routingKey) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Регистрация {} сообщений на отложенную отправку", messages.size());

            messages.forEach(message -> delayedMessageDispatcher.enqueueSingle(
                    message,
                    routingKey != null ? routingKey : "message.routing.key"
            ));

            return messages;
        }, executorService);
    }

    /**
     * Проверяет статус каналов перед отправкой
     */
    public boolean canSendMessages() {
        return channelMonitorService.areChannelsAvailable();
    }

    /**
     * Получает статус каналов
     */
    public String getChannelStatus() {
        return channelMonitorService.getChannelStatus().toString();
    }
}