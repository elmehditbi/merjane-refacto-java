package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NormalProductProcessor implements ProductProcessor {
    private final ProductService productService;
    private final ProductRepository productRepository;

    public NormalProductProcessor(@Lazy ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @Override
    public void process(Product product) {
        log.info("Start Service process product by type {} and id {}", product.getType(), product.getId());
        if (product.hasStock()) {
            product.decrementStock();
            productRepository.save(product);
        } else if (product.getLeadTime() > 0) {
            productService.notifyDelay(product.getLeadTime(), product);
        }
        log.info("End Service process product by type {} and id {}", product.getType(), product.getId());
    }
}
