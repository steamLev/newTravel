package com.example.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для маршрутизации кешей
 * Позволяет указать какие кеши использовать для конкретных операций
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheRoute {
    
    /**
     * Типы кешей для использования
     */
    CacheType[] value() default {CacheType.L1, CacheType.L2};
    
    /**
     * Имя кеша (опционально)
     */
    String cacheName() default "";
    
    /**
     * TTL в секундах (опционально, переопределяет конфигурацию)
     */
    int ttl() default -1;
    
    /**
     * Типы кешей
     */
    enum CacheType {
        L1,    // Caffeine (локальный кеш)
        L2,    // Redis (распределенный кеш)
        BOTH   // Оба кеша
    }
}