package com.example.rabbitmq.test;

import com.example.rabbitmq.constants.ConstantsRMQ;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Тест для проверки работы @RabbitListener
 */
@Component
public class RabbitListenerTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Отправляет тестовое сообщение в BINARY_QUEUE
     */
    public void sendTestMessage() {
        try {
            String testMessage = "Тестовое сообщение для конвертации";
            byte[] messageBytes = testMessage.getBytes();
            
            System.out.println("📤 Отправляем тестовое сообщение в очередь: " + ConstantsRMQ.BINARY_QUEUE);
            System.out.println("📏 Размер сообщения: " + messageBytes.length + " байт");
            
            // Отправляем сообщение в очередь
            rabbitTemplate.convertAndSend(
                ConstantsRMQ.BINARY_EXCHANGE, 
                ConstantsRMQ.BINARY_ROUTING_KEY, 
                messageBytes
            );
            
            System.out.println("✅ Сообщение успешно отправлено!");
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка при отправке сообщения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}