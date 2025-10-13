package com.example.rabbitmq.listener;

import com.example.rabbitmq.constants.ConstantsRMQ;
import com.example.rabbitmq.service.ConversionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Слушатель для конвертации документов
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentConversionListener {

    private final ConversionService conversionService;

    /**
     * Слушатель для конвертации документов в PDF
     * @param file исходный файл в виде byte[]
     * @return PDF файл в виде byte[]
     */
    @RabbitListener(queues = ConstantsRMQ.BINARY_QUEUE, concurrency = "2")
    public byte[] convertToPdfListener(byte[] file) {
        log.info("Получен файл для конвертации в PDF. Размер: {} байт", file.length);
        
        try {
            byte[] result = conversionService.convertDocumentToPdf(file);
            log.info("Конвертация завершена успешно. Результат: {} байт", result.length);
            return result;
            
        } catch (Exception e) {
            log.error("Ошибка при конвертации файла: {}", e.getMessage(), e);
            throw e; // Перебрасываем исключение для повторной обработки
        }
    }
}