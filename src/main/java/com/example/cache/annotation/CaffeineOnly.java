package com.example.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для маршрутизации только в Caffeine
 * Используется для данных, которые должны быть только в локальном кеше
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@CacheRoute({CacheRoute.CacheType.L1})
public @interface CaffeineOnly {
    
    /**
     * Имя кеша в Caffeine
     */
    String cacheName() default "caffeine_cache";
    
    /**
     * TTL в секундах
     */
    int ttl() default -1;
}