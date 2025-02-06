package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.entities.*;
import com.nimbleways.springboilerplate.repositories.*;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void processOrder_WithNormalProduct_ShouldProcess() throws Exception {
        Product product = createProduct("Normal Product", Product.ProductType.NORMAL, 5);
        Order order = createOrderWithProducts(Set.of(product));

         mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()));

        Product updatedProduct = productRepository.findById(order.getId()).get();
        assertEquals(4, updatedProduct.getAvailable());
    }

    @Test
    void processOrder_WithExpiredProduct_ShouldNotifyAndNotProcess() throws Exception {
        Product product = createExpirableProduct("Expired Product", 5, LocalDate.now().minusDays(1));
        Order order = createOrderWithProducts(Set.of(product));

        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId()))
                .andExpect(status().isOk());

        verify(notificationService).sendExpirationNotification(product.getName(), LocalDate.now().minusDays(1));
        Product updatedProduct = productRepository.findById(order.getId()).get();
        assertEquals(0, updatedProduct.getAvailable());
    }

    @Test
    void processOrder_WithSeasonalProduct_InSeason_ShouldProcess() throws Exception {
        Product product = createSeasonalProduct("Seasonal Product", 5,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(10));
        Order order = createOrderWithProducts(Set.of(product));

        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId()))
                .andExpect(status().isOk());

        Product updatedProduct = productRepository.findById(order.getId()).get();
        assertEquals(4, updatedProduct.getAvailable());
    }

    private Product createProduct(String name, Product.ProductType type, int quantity) {
        Product product = new Product();
        product.setName(name);
        product.setType(type);
        product.setAvailable(quantity);
        return productRepository.save(product);
    }

    private Product createExpirableProduct(String name, int quantity, LocalDate expiryDate) {
        Product product = createProduct(name, Product.ProductType.EXPIRABLE, quantity);
        product.setExpiryDate(expiryDate);
        return productRepository.save(product);
    }

    private Product createSeasonalProduct(String name, int quantity, LocalDate startDate, LocalDate endDate) {
        Product product = createProduct(name, Product.ProductType.SEASONAL, quantity);
        product.setSeasonStartDate(startDate);
        product.setSeasonEndDate(endDate);
        return productRepository.save(product);
    }

    private Order createOrderWithProducts(Set<Product> products) {
        Order order = new Order();
        order.setItems(products);
        return orderRepository.save(order);
    }
}
