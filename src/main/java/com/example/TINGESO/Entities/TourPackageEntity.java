package com.example.TINGESO.Entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "tour_packages")
@Data
public class TourPackageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String destination;

    @Column(length = 2000, nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer durationDays;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer totalSlots;

    @Column(nullable = false)
    private Integer availableSlots;

    // Relación para tener multiples servicios en un solo paquete
    @ElementCollection(targetClass = ServiceIncludedEnum.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "package_services", joinColumns = @JoinColumn(name = "package_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "service")
    private Set<ServiceIncludedEnum> includedServices;

    @Column(length = 1000)
    private String conditions;

    @Column(length = 1000)
    private String restrictions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripTypeEnum tripType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeasonEnum season;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryEnum category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageStatusEnum status;

    @Column(nullable = false)
    private Boolean isVisible;
}
