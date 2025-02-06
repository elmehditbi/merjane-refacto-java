package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExpirableProductProcessor implements ProductProcessor {
    private final ProductRepository productRepository;
    private final ProductService productService;

    public ExpirableProductProcessor(ProductRepository productRepository, @Lazy ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @Override
    public void process(Product product) {
        log.info("Start Service process product by type {} and id {}", product.getType(), product.getId());
        if (product.hasStock() && product.isExpiredAndValid()) {
            product.decrementStock();
            productRepository.save(product);
        } else {
            productService.handleExpiredProduct(product);
        }
        log.info("End Service process product by type {} and id {}", product.getType(), product.getId());
    }
}
