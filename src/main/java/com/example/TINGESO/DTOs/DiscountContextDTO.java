package com.example.TINGESO.DTOs;

import lombok.Data;

@Data
public class DiscountContextDTO {
    private Double finalDiscountPercentage = 0.0;
    private String appliedDiscountsDetails = "";
}
