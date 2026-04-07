package com.example.TINGESO.DTOs;

import com.example.TINGESO.Entities.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class TourPackageRequest {
    private String name;
    private String destination;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double price;
    private Integer totalSlots;
    private Set<ServiceIncludedEnum> includedServices;
    private String conditions;
    private String restrictions;
    private TripTypeEnum tripType;
    private SeasonEnum season;
    private CategoryEnum category;
    private PackageStatusEnum status; // Opcional (por defecto es DISPONIBLE)
    private Boolean isVisible; // Opcional (por defecto true)
}
