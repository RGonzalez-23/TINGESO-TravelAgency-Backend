package com.example.TINGESO.Entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity representing a simulated payment transaction.
 * Follows Epic 5 guidelines: ONE payment per reservation.
 * Sensitive data such as CCV or CardNumber are strictly EXCLUDED from this entity.
 */
@Entity
@Table(name = "payment_transactions")
@Data
public class PaymentTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A unique hash to simulate an external gateway payment voucher ID.
    @Column(nullable = false, unique = true)
    private String transactionHash;

    @Column(nullable = false)
    private Double amountPaid;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    // E.g., "CREDIT", "DEBIT"
    @Column(nullable = false)
    private String paymentMethod;

    // OneToOne relationship ensures a Reservation has strictly ONE transaction.
    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private ReservationEntity reservation;
}
