package com.example.TINGESO.DTOs;

import lombok.Data;

@Data
public class PassengerDTO {
    private String fullName;
    private Integer age;
    private Boolean needsAssistance;
    private String assistanceDetails;
}
