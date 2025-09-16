package com.example.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для двухуровневого кеширования
 * Используется для данных, которые должны быть в обоих кешах
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@CacheRoute({CacheRoute.CacheType.L1, CacheRoute.CacheType.L2})
public @interface TwoLevelCache {
    
    /**
     * Имя кеша
     */
    String cacheName() default "two_level_cache";
    
    /**
     * TTL в секундах для L1 кеша
     */
    int l1Ttl() default -1;
    
    /**
     * TTL в секундах для L2 кеша
     */
    int l2Ttl() default -1;
}