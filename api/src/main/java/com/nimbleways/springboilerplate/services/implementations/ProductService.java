package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final Map<Product.ProductType, ProductProcessor> processors;

    @Autowired
    public ProductService(ProductRepository productRepository, NotificationService notificationService, List<ProductProcessor> productProcessors) {

        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.processors = new EnumMap<>(Product.ProductType.class);

        productProcessors.forEach(processor -> {
            String name = processor.getClass().getSimpleName();
            if (name.contains("Normal")) {
                processors.put(Product.ProductType.NORMAL, processor);
            } else if (name.contains("Seasonal")) {
                processors.put(Product.ProductType.SEASONAL, processor);
            } else if (name.contains("Expirable")) {
                processors.put(Product.ProductType.EXPIRABLE, processor);
            }
        });
    }

    public void processProduct(Product product) {
        log.info("Start Service process product by id {}", product.getId());
        ProductProcessor processor = processors.get(product.getType());
        if (processor != null) {
            processor.process(product);
        }
        log.info("End Service process product by id {}", product.getId());
    }

    public void notifyDelay(int leadTime, Product product) {
        product.setLeadTime(leadTime);
        productRepository.save(product);
        notificationService.sendDelayNotification(leadTime, product.getName());
    }

    public void handleSeasonalProduct(Product product) {
        if (LocalDate.now().plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate())) {
            notificationService.sendOutOfStockNotification(product.getName());
            product.setAvailable(0);
            productRepository.save(product);
        } else if (product.getSeasonStartDate().isAfter(LocalDate.now())) {
            notificationService.sendOutOfStockNotification(product.getName());
            productRepository.save(product);
        } else {
            notifyDelay(product.getLeadTime(), product);
        }
    }

    public void handleExpiredProduct(Product product) {
        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now())) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
        } else {
            notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
            product.setAvailable(0);
            productRepository.save(product);
        }
    }
}
