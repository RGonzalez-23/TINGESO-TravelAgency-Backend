package com.example.TINGESO.DTOs;

import com.example.TINGESO.Entities.CategoryEnum;
import com.example.TINGESO.Entities.PackageStatusEnum;
import com.example.TINGESO.Entities.TourPackageEntity;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class TourPackageFilterDTO {
    
    private String destination;
    private Double minPrice;
    private Double maxPrice;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;
    
    private CategoryEnum category;
    private Integer minDuration;
    private Integer maxDuration;

    // Centralizamos la lógica de construcción de Specification dentro del DTO
    public Specification<TourPackageEntity> toSpecification() {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Reglas estrictas de Negocio de Épica 3:
            predicates.add(criteriaBuilder.equal(root.get("isVisible"), true));
            predicates.add(criteriaBuilder.greaterThan(root.get("startDate"), LocalDate.now()));
            predicates.add(criteriaBuilder.notEqual(root.get("status"), PackageStatusEnum.CANCELADO));
            predicates.add(criteriaBuilder.notEqual(root.get("status"), PackageStatusEnum.NO_VIGENTE));

            // 2. Filtros dinámicos
            if (destination != null && !destination.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("destination")), "%" + destination.toLowerCase() + "%"));
            }
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (dateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), dateTo));
            }
            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }
            if (minDuration != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("durationDays"), minDuration));
            }
            if (maxDuration != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("durationDays"), maxDuration));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
