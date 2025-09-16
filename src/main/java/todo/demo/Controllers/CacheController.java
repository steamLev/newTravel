package todo.demo.Controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import todo.demo.Models.ClientRiskDto;
import todo.demo.Services.ClientRiskService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для демонстрации работы с системой маршрутизации кэша
 */
@Slf4j
@RestController
@RequestMapping("/api/cache")
public class CacheController {
    
    private final ClientRiskService clientRiskService;
    
    @Autowired
    public CacheController(ClientRiskService clientRiskService) {
        this.clientRiskService = clientRiskService;
    }
    
    /**
     * Устанавливает значение в кэш
     */
    @PostMapping("/set")
    public ResponseEntity<Map<String, Object>> setValue(@RequestParam String key, 
                                                       @RequestBody ClientRiskDto value) {
        try {
            clientRiskService.setValue(key, value);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Value set successfully");
            response.put("key", key);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error setting value", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error setting value: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Получает значение из кэша
     */
    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getValue(@RequestParam String key) {
        try {
            ClientRiskDto value = clientRiskService.getValue(key);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("key", key);
            response.put("value", value);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting value", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error getting value: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Получает все записи с ошибками
     */
    @GetMapping("/errors")
    public ResponseEntity<Map<String, Object>> getErrorValues() {
        try {
            List<ClientRiskDto> errors = clientRiskService.getErrorValue();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", errors.size());
            response.put("errors", errors);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting error values", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error getting error values: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Получает статистику кэша
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        try {
            String stats = clientRiskService.getCacheStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting cache stats", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error getting cache stats: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Проверяет существование ключа
     */
    @GetMapping("/has-key")
    public ResponseEntity<Map<String, Object>> hasKey(@RequestParam String key) {
        try {
            boolean exists = clientRiskService.hasKey(key);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("key", key);
            response.put("exists", exists);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking key existence", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error checking key existence: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Удаляет значение из кэша
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteValue(@RequestParam String key) {
        try {
            clientRiskService.deleteValue(key);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Value deleted successfully");
            response.put("key", key);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting value", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error deleting value: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Очищает весь кэш
     */
    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        try {
            clientRiskService.clearCache();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cache cleared successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error clearing cache", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error clearing cache: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}