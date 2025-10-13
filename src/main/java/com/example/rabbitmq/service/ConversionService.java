package com.example.rabbitmq.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Сервис для конвертации документов
 */
@Slf4j
@Service
public class ConversionService {

    /**
     * Конвертирует документ в PDF
     * @param file исходный файл в виде byte[]
     * @return PDF файл в виде byte[]
     */
    public byte[] convertDocumentToPdf(byte[] file) {
        log.info("Начинаем конвертацию документа в PDF. Размер файла: {} байт", file.length);
        
        try {
            // Здесь должна быть логика конвертации
            // Пока что просто возвращаем исходный файл
            log.info("Конвертация завершена успешно");
            return file;
            
        } catch (Exception e) {
            log.error("Ошибка при конвертации документа: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка конвертации", e);
        }
    }
}