package com.example.cache.service;

import com.example.cache.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Сервис для работы с пользователями с использованием двухуровневого кеша
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String CACHE_NAME = "users";

    @Autowired
    private TwoLevelCacheService cacheService;

    // Имитация базы данных
    private final List<User> users = new ArrayList<>();

    public UserService() {
        // Инициализация тестовых данных
        users.add(new User(1L, "Иван", "Иванов", "ivan@example.com"));
        users.add(new User(2L, "Петр", "Петров", "petr@example.com"));
        users.add(new User(3L, "Мария", "Сидорова", "maria@example.com"));
        users.add(new User(4L, "Анна", "Козлова", "anna@example.com"));
        users.add(new User(5L, "Сергей", "Смирнов", "sergey@example.com"));
    }

    /**
     * Получить пользователя по ID с кешированием
     */
    public User getUserById(Long id) {
        String cacheKey = "user:" + id;
        
        return cacheService.get(CACHE_NAME, cacheKey, User.class, () -> {
            logger.info("Loading user from database: {}", id);
            // Имитация задержки базы данных
            simulateDatabaseDelay();
            return users.stream()
                    .filter(user -> user.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        });
    }

    /**
     * Получить всех пользователей с кешированием
     */
    public List<User> getAllUsers() {
        String cacheKey = "all_users";
        
        return cacheService.get(CACHE_NAME, cacheKey, List.class, () -> {
            logger.info("Loading all users from database");
            // Имитация задержки базы данных
            simulateDatabaseDelay();
            return new ArrayList<>(users);
        });
    }

    /**
     * Создать нового пользователя
     */
    public User createUser(User user) {
        logger.info("Creating new user: {}", user.getEmail());
        simulateDatabaseDelay();
        
        user.setId((long) (users.size() + 1));
        users.add(user);
        
        // Очищаем кеш списка всех пользователей
        cacheService.evict(CACHE_NAME, "all_users");
        
        return user;
    }

    /**
     * Обновить пользователя
     */
    public User updateUser(Long id, User updatedUser) {
        logger.info("Updating user: {}", id);
        simulateDatabaseDelay();
        
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(id)) {
                updatedUser.setId(id);
                users.set(i, updatedUser);
                
                // Очищаем кеш конкретного пользователя и списка всех пользователей
                cacheService.evict(CACHE_NAME, "user:" + id);
                cacheService.evict(CACHE_NAME, "all_users");
                
                return updatedUser;
            }
        }
        return null;
    }

    /**
     * Удалить пользователя
     */
    public boolean deleteUser(Long id) {
        logger.info("Deleting user: {}", id);
        simulateDatabaseDelay();
        
        boolean removed = users.removeIf(user -> user.getId().equals(id));
        if (removed) {
            // Очищаем кеш конкретного пользователя и списка всех пользователей
            cacheService.evict(CACHE_NAME, "user:" + id);
            cacheService.evict(CACHE_NAME, "all_users");
        }
        return removed;
    }

    /**
     * Очистить весь кеш пользователей
     */
    public void clearUserCache() {
        logger.info("Clearing user cache");
        cacheService.clear(CACHE_NAME);
    }

    /**
     * Имитация задержки базы данных
     */
    private void simulateDatabaseDelay() {
        try {
            TimeUnit.MILLISECONDS.sleep(100); // 100ms задержка
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}