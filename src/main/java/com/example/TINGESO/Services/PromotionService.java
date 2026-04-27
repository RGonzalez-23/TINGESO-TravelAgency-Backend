package com.example.TINGESO.Services;

import com.example.TINGESO.Entities.PromotionEntity;
import com.example.TINGESO.Repositories.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    public List<PromotionEntity> getAllPromotions() {
        return promotionRepository.findAll();
    }

    @Transactional
    public PromotionEntity createPromotion(PromotionEntity promotion) {
        if (promotion.getStartDate().isAfter(promotion.getEndDate())) {
            throw new RuntimeException("La fecha de inicio no puede ser posterior a la de término.");
        }
        if (promotion.getDiscountPercentage() <= 0 || promotion.getDiscountPercentage() > 100) {
            throw new RuntimeException("El porcentaje de descuento debe estar entre 1 y 100.");
        }
        return promotionRepository.save(promotion);
    }

    @Transactional
    public PromotionEntity updatePromotion(Long id, PromotionEntity promoDetails) {
        PromotionEntity promo = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoción no encontrada"));

        if (promoDetails.getStartDate().isAfter(promoDetails.getEndDate())) {
            throw new RuntimeException("La fecha de inicio no puede ser posterior a la de término.");
        }
        if (promoDetails.getDiscountPercentage() <= 0 || promoDetails.getDiscountPercentage() > 100) {
            throw new RuntimeException("El porcentaje de descuento debe estar entre 1 y 100.");
        }

        promo.setName(promoDetails.getName());
        promo.setStartDate(promoDetails.getStartDate());
        promo.setEndDate(promoDetails.getEndDate());
        promo.setDiscountPercentage(promoDetails.getDiscountPercentage());
        promo.setIsActive(promoDetails.getIsActive());

        return promotionRepository.save(promo);
    }

    @Transactional
    public void deletePromotion(Long id) {
        PromotionEntity promo = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoción no encontrada"));
        promo.setIsActive(false); // Soft Delete
        promotionRepository.save(promo);
    }
}
