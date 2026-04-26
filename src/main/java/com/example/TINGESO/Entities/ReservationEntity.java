package com.example.TINGESO.Entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
@Data
public class ReservationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String keycloakUserId; // El ID de usuario extraido del contexto de seguridad

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tour_package_id", nullable = false)
    private TourPackageEntity tourPackage;

    @Column(nullable = false)
    private Integer passengersCount;

    @Column(nullable = false)
    private Double totalAmount; // Monto sin descuentos

    @Column(nullable = false)
    private Double finalAmount; // Monto tras los descuentos

    @Column(nullable = false)
    private Double discountPercentage; // El porcentaje descontado sumado

    @Column(length = 2000)
    private String appliedDiscountsDetails; // Razones de los descuentos aplicados ej: "10% Grupo, 5% MultiPaquete"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatusEnum status;

    @Column(length = 1000)
    private String specialRequests;

    @Column(length = 1000)
    private String preferences;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime paidAt; // Para validar compras de multi-paquete posteriores o clientes frecuentes

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PassengerEntity> passengers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
