package com.example.cache.controller;

import com.example.cache.model.Product;
import com.example.cache.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST контроллер для работы с продуктами
 * Демонстрирует различные сценарии кеширования
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    /**
     * Получить продукт по ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        logger.info("Getting product by ID: {}", id);
        
        long startTime = System.currentTimeMillis();
        Product product = productService.getProductById(id);
        long endTime = System.currentTimeMillis();
        
        logger.info("Product retrieval took {} ms", endTime - startTime);
        
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Получить все продукты
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        logger.info("Getting all products");
        
        long startTime = System.currentTimeMillis();
        List<Product> products = productService.getAllProducts();
        long endTime = System.currentTimeMillis();
        
        logger.info("All products retrieval took {} ms", endTime - startTime);
        
        return ResponseEntity.ok(products);
    }

    /**
     * Получить продукты по категории
     * GET /api/products/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        logger.info("Getting products by category: {}", category);
        
        long startTime = System.currentTimeMillis();
        List<Product> products = productService.getProductsByCategory(category);
        long endTime = System.currentTimeMillis();
        
        logger.info("Products by category retrieval took {} ms", endTime - startTime);
        
        return ResponseEntity.ok(products);
    }

    /**
     * Получить продукты в ценовом диапазоне
     * GET /api/products/price-range?min={min}&max={max}
     */
    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        logger.info("Getting products by price range: {} - {}", min, max);
        
        long startTime = System.currentTimeMillis();
        List<Product> products = productService.getProductsByPriceRange(min, max);
        long endTime = System.currentTimeMillis();
        
        logger.info("Products by price range retrieval took {} ms", endTime - startTime);
        
        return ResponseEntity.ok(products);
    }

    /**
     * Поиск продуктов по названию
     * GET /api/products/search?q={query}
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
        logger.info("Searching products: {}", q);
        
        long startTime = System.currentTimeMillis();
        List<Product> products = productService.searchProducts(q);
        long endTime = System.currentTimeMillis();
        
        logger.info("Product search took {} ms", endTime - startTime);
        
        return ResponseEntity.ok(products);
    }

    /**
     * Создать новый продукт
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        logger.info("Creating new product: {}", product.getName());
        
        long startTime = System.currentTimeMillis();
        Product createdProduct = productService.createProduct(product);
        long endTime = System.currentTimeMillis();
        
        logger.info("Product creation took {} ms", endTime - startTime);
        
        return ResponseEntity.ok(createdProduct);
    }

    /**
     * Обновить продукт
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        logger.info("Updating product: {}", id);
        
        long startTime = System.currentTimeMillis();
        Product updatedProduct = productService.updateProduct(id, product);
        long endTime = System.currentTimeMillis();
        
        logger.info("Product update took {} ms", endTime - startTime);
        
        if (updatedProduct != null) {
            return ResponseEntity.ok(updatedProduct);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Удалить продукт
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.info("Deleting product: {}", id);
        
        long startTime = System.currentTimeMillis();
        boolean deleted = productService.deleteProduct(id);
        long endTime = System.currentTimeMillis();
        
        logger.info("Product deletion took {} ms", endTime - startTime);
        
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Очистить кеш продуктов
     * POST /api/products/cache/clear
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<String> clearProductCache() {
        logger.info("Clearing product cache");
        
        long startTime = System.currentTimeMillis();
        productService.clearProductCache();
        long endTime = System.currentTimeMillis();
        
        logger.info("Cache clearing took {} ms", endTime - startTime);
        
        return ResponseEntity.ok("Product cache cleared successfully");
    }
}