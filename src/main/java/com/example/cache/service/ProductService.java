package com.example.cache.service;

import com.example.cache.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Сервис для работы с продуктами с использованием двухуровневого кеша
 * Демонстрирует более сложные сценарии кеширования
 */
@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private static final String CACHE_NAME = "products";

    @Autowired
    private TwoLevelCacheService cacheService;

    // Имитация базы данных
    private final List<Product> products = new ArrayList<>();

    public ProductService() {
        // Инициализация тестовых данных
        products.add(new Product(1L, "Ноутбук", "Электроника", new BigDecimal("50000.00"), 10));
        products.add(new Product(2L, "Смартфон", "Электроника", new BigDecimal("30000.00"), 25));
        products.add(new Product(3L, "Книга", "Книги", new BigDecimal("500.00"), 100));
        products.add(new Product(4L, "Стул", "Мебель", new BigDecimal("5000.00"), 5));
        products.add(new Product(5L, "Стол", "Мебель", new BigDecimal("15000.00"), 3));
    }

    /**
     * Получить продукт по ID с кешированием
     */
    public Product getProductById(Long id) {
        String cacheKey = "product:" + id;
        
        return cacheService.get(CACHE_NAME, cacheKey, Product.class, () -> {
            logger.info("Loading product from database: {}", id);
            simulateDatabaseDelay();
            return products.stream()
                    .filter(product -> product.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        });
    }

    /**
     * Получить продукты по категории с кешированием
     */
    public List<Product> getProductsByCategory(String category) {
        String cacheKey = "category:" + category;
        
        return cacheService.get(CACHE_NAME, cacheKey, List.class, () -> {
            logger.info("Loading products by category from database: {}", category);
            simulateDatabaseDelay();
            return products.stream()
                    .filter(product -> product.getCategory().equals(category))
                    .collect(Collectors.toList());
        });
    }

    /**
     * Получить продукты в ценовом диапазоне с кешированием
     */
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        String cacheKey = String.format("price_range:%.2f-%.2f", minPrice, maxPrice);
        
        return cacheService.get(CACHE_NAME, cacheKey, List.class, () -> {
            logger.info("Loading products by price range from database: {} - {}", minPrice, maxPrice);
            simulateDatabaseDelay();
            return products.stream()
                    .filter(product -> product.getPrice().compareTo(minPrice) >= 0 && 
                                     product.getPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        });
    }

    /**
     * Поиск продуктов по названию с кешированием
     */
    public List<Product> searchProducts(String searchTerm) {
        String cacheKey = "search:" + searchTerm.toLowerCase();
        
        return cacheService.get(CACHE_NAME, cacheKey, List.class, () -> {
            logger.info("Searching products from database: {}", searchTerm);
            simulateDatabaseDelay();
            return products.stream()
                    .filter(product -> product.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
        });
    }

    /**
     * Получить все продукты с кешированием
     */
    public List<Product> getAllProducts() {
        String cacheKey = "all_products";
        
        return cacheService.get(CACHE_NAME, cacheKey, List.class, () -> {
            logger.info("Loading all products from database");
            simulateDatabaseDelay();
            return new ArrayList<>(products);
        });
    }

    /**
     * Создать новый продукт
     */
    public Product createProduct(Product product) {
        logger.info("Creating new product: {}", product.getName());
        simulateDatabaseDelay();
        
        product.setId((long) (products.size() + 1));
        products.add(product);
        
        // Очищаем связанные кеши
        clearRelatedCaches();
        
        return product;
    }

    /**
     * Обновить продукт
     */
    public Product updateProduct(Long id, Product updatedProduct) {
        logger.info("Updating product: {}", id);
        simulateDatabaseDelay();
        
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(id)) {
                updatedProduct.setId(id);
                products.set(i, updatedProduct);
                
                // Очищаем связанные кеши
                clearRelatedCaches();
                
                return updatedProduct;
            }
        }
        return null;
    }

    /**
     * Удалить продукт
     */
    public boolean deleteProduct(Long id) {
        logger.info("Deleting product: {}", id);
        simulateDatabaseDelay();
        
        boolean removed = products.removeIf(product -> product.getId().equals(id));
        if (removed) {
            // Очищаем связанные кеши
            clearRelatedCaches();
        }
        return removed;
    }

    /**
     * Очистить весь кеш продуктов
     */
    public void clearProductCache() {
        logger.info("Clearing product cache");
        cacheService.clear(CACHE_NAME);
    }

    /**
     * Очистить связанные кеши при изменении данных
     */
    private void clearRelatedCaches() {
        // Очищаем кеш всех продуктов
        cacheService.evict(CACHE_NAME, "all_products");
        
        // Очищаем кеши по категориям (в реальном приложении можно использовать более умную стратегию)
        cacheService.evict(CACHE_NAME, "category:Электроника");
        cacheService.evict(CACHE_NAME, "category:Книги");
        cacheService.evict(CACHE_NAME, "category:Мебель");
        
        // Очищаем кеши поиска (в реальном приложении можно использовать паттерн или TTL)
        // Здесь для простоты очищаем все кеши поиска
        // В production можно использовать Redis SCAN для поиска ключей по паттерну
    }

    /**
     * Имитация задержки базы данных
     */
    private void simulateDatabaseDelay() {
        try {
            TimeUnit.MILLISECONDS.sleep(150); // 150ms задержка
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}