package com.example.cache.controller;

import com.example.cache.model.User;
import com.example.cache.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для работы с пользователями
 * Демонстрирует работу двухуровневого кеша
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * Получить пользователя по ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        logger.info("Getting user by ID: {}", id);
        
        long startTime = System.currentTimeMillis();
        User user = userService.getUserById(id);
        long endTime = System.currentTimeMillis();
        
        logger.info("User retrieval took {} ms", endTime - startTime);
        
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Получить всех пользователей
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Getting all users");
        
        long startTime = System.currentTimeMillis();
        List<User> users = userService.getAllUsers();
        long endTime = System.currentTimeMillis();
        
        logger.info("All users retrieval took {} ms", endTime - startTime);
        
        return ResponseEntity.ok(users);
    }

    /**
     * Создать нового пользователя
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        logger.info("Creating new user: {}", user.getEmail());
        
        long startTime = System.currentTimeMillis();
        User createdUser = userService.createUser(user);
        long endTime = System.currentTimeMillis();
        
        logger.info("User creation took {} ms", endTime - startTime);
        
        return ResponseEntity.ok(createdUser);
    }

    /**
     * Обновить пользователя
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        logger.info("Updating user: {}", id);
        
        long startTime = System.currentTimeMillis();
        User updatedUser = userService.updateUser(id, user);
        long endTime = System.currentTimeMillis();
        
        logger.info("User update took {} ms", endTime - startTime);
        
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Удалить пользователя
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user: {}", id);
        
        long startTime = System.currentTimeMillis();
        boolean deleted = userService.deleteUser(id);
        long endTime = System.currentTimeMillis();
        
        logger.info("User deletion took {} ms", endTime - startTime);
        
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Очистить кеш пользователей
     * POST /api/users/cache/clear
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<String> clearUserCache() {
        logger.info("Clearing user cache");
        
        long startTime = System.currentTimeMillis();
        userService.clearUserCache();
        long endTime = System.currentTimeMillis();
        
        logger.info("Cache clearing took {} ms", endTime - startTime);
        
        return ResponseEntity.ok("User cache cleared successfully");
    }

    /**
     * Получить статистику кеша (заглушка)
     * GET /api/users/cache/stats
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<String> getCacheStats() {
        return ResponseEntity.ok("Cache statistics endpoint - implement if needed");
    }
}