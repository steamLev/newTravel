package com.example.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для маршрутизации только в Redis
 * Используется для AmlClient и других данных, которые должны быть только в Redis
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@CacheRoute({CacheRoute.CacheType.L2})
public @interface RedisOnly {
    
    /**
     * Имя кеша в Redis
     */
    String cacheName() default "redis_cache";
    
    /**
     * TTL в секундах
     */
    int ttl() default -1;
}