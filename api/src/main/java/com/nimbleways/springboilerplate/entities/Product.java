package com.nimbleways.springboilerplate.entities;

import lombok.*;

import java.time.LocalDate;

import javax.persistence.*;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {
    public enum ProductType {
        NORMAL,
        SEASONAL,
        EXPIRABLE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "lead_time")
    private Integer leadTime;

    @Column(name = "available")
    private Integer available;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ProductType type;

    @Column(name = "name")
    private String name;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "season_start_date")
    private LocalDate seasonStartDate;

    @Column(name = "season_end_date")
    private LocalDate seasonEndDate;

    public boolean hasStock() {
        return available > 0;
    }

    public void decrementStock() {
        if (hasStock()) {
            available--;
        }
    }

    public boolean isInSeason() {
        LocalDate now = LocalDate.now();
        return now.isAfter(seasonStartDate) && now.isBefore(seasonEndDate);
    }

    public boolean isExpiredAndValid() {
        LocalDate now = LocalDate.now();
        return expiryDate.isAfter(now);
    }

}
