package com.example.TINGESO.DTOs;

import com.example.TINGESO.Entities.ReservationStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReservationResponseDTO {
    private Long id;
    private Long packageId;
    private String packageName;
    private String destination;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String keycloakUserId;
    private Integer passengersCount;
    private Double totalAmount;
    private Double finalAmount;
    private Double discountPercentage;
    private String appliedDiscountsDetails;
    private ReservationStatusEnum status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private List<PassengerDTO> passengers;
}
