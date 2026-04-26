package com.example.TINGESO.Repositories;

import com.example.TINGESO.Entities.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {
    // Buscar promociones activas actuales
    List<PromotionEntity> findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(LocalDateTime now1, LocalDateTime now2);
}
