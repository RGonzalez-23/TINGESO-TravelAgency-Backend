package com.example.TINGESO.DTOs;

import lombok.Data;
import java.util.List;

@Data
public class ReservationRequestDTO {
    private Long tourPackageId;
    private Integer passengersCount;
    private List<PassengerDTO> passengers;
    private String specialRequests;
    private String preferences;
}
