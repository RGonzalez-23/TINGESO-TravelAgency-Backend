package com.example.TINGESO.Entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "global_discount_config")
@Data
public class GlobalDiscountConfigEntity {
    @Id
    private Long id = 1L; // Solo debe existir 1 registro de config en todo el sistema

    @Column(nullable = false)
    private Integer groupMinPassengers = 4;

    @Column(nullable = false)
    private Double groupDiscountPercentage = 10.0;

    @Column(nullable = false)
    private Integer frequentClientMinReservations = 3;

    @Column(nullable = false)
    private Double frequentClientDiscountPercentage = 10.0;

    @Column(nullable = false)
    private Integer multiPackageDaysWindow = 7;

    @Column(nullable = false)
    private Double multiPackageDiscountPercentage = 5.0;

    // Límite máximo global según regla de negocio
    @Column(nullable = false)
    private Double maxGlobalDiscountPercentageCap = 35.0;

    @Column(nullable = false)
    private Boolean areDiscountsAccumulative = false;
}
