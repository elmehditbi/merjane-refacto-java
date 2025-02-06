package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@UnitTest
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ExpirableProductProcessor expirableProcessor;

    @Mock
    private NormalProductProcessor normalProcessor;

    @Mock
    private SeasonalProductProcessor seasonalProcessor;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        List<ProductProcessor> processors = Arrays.asList(
                expirableProcessor,
                normalProcessor,
                seasonalProcessor
        );

        productService = new ProductService(
                productRepository,
                notificationService,
                processors
        );
    }

    @Test
    void handleExpiredProduct_WhenValid_ShouldDecrementStock() {
        Product product = createProduct(5, LocalDate.now().plusDays(10));

        productService.handleExpiredProduct(product);

        assertEquals(4, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void handleExpiredProduct_WhenExpired_ShouldNotifyAndSetZeroStock() {
        Product product = createProduct(5, LocalDate.now().minusDays(1));

        productService.handleExpiredProduct(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository).save(product);
        verify(notificationService).sendExpirationNotification(product.getName(), product.getExpiryDate());
    }

    @Test
    void handleExpiredProduct_WhenNoStock_ShouldNotify() {
        Product product = createProduct(0, LocalDate.now().plusDays(10));

        productService.handleExpiredProduct(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository).save(product);
        verify(notificationService).sendExpirationNotification(product.getName(), product.getExpiryDate());
    }

    @Test
    void handleSeasonalProduct_InSeason_WithStock_ShouldProcess() {
        Product product = createSeasonalProduct(5,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(10));

        productService.handleSeasonalProduct(product);

        assertEquals(5, product.getAvailable());
        verify(productRepository).save(product);
    }

    @Test
    void handleSeasonalProduct_OutOfSeason_ShouldNotify() {
        Product product = createSeasonalProduct(5,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(10));

        productService.handleSeasonalProduct(product);

        verify(notificationService).sendOutOfStockNotification(product.getName());
        verify(productRepository).save(product);
    }

    private Product createProduct(int stock, LocalDate expiryDate) {
        Product product = new Product();
        product.setName("Test Product");
        product.setAvailable(stock);
        product.setExpiryDate(expiryDate);
        return product;
    }

    private Product createSeasonalProduct(int stock, LocalDate startDate, LocalDate endDate) {
        Product product = new Product();
        product.setName("Seasonal Product");
        product.setAvailable(stock);
        product.setLeadTime(5);
        product.setSeasonStartDate(startDate);
        product.setSeasonEndDate(endDate);
        return product;
    }
}
