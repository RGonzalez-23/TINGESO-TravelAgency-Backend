package com.example.TINGESO.Services;

import com.example.TINGESO.Entities.PromotionEntity;
import com.example.TINGESO.Repositories.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private PromotionService promotionService;

    @Test
    void createPromotion_Success() {
        PromotionEntity promo = new PromotionEntity();
        promo.setStartDate(LocalDateTime.now().minusDays(1));
        promo.setEndDate(LocalDateTime.now().plusDays(1));
        promo.setDiscountPercentage(50.0);

        when(promotionRepository.save(any(PromotionEntity.class))).thenReturn(promo);

        PromotionEntity result = promotionService.createPromotion(promo);
        assertThat(result.getDiscountPercentage()).isEqualTo(50.0);
    }

    @Test
    void createPromotion_InvalidDates_ThrowsException() {
        PromotionEntity promo = new PromotionEntity();
        promo.setStartDate(LocalDateTime.now().plusDays(1));
        promo.setEndDate(LocalDateTime.now().minusDays(1));

        assertThrows(RuntimeException.class, () -> promotionService.createPromotion(promo));
    }

    @Test
    void deletePromotion_SoftDeletes() {
        PromotionEntity promo = new PromotionEntity();
        promo.setIsActive(true);
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promo));

        promotionService.deletePromotion(1L);

        verify(promotionRepository).save(promo);
        assertThat(promo.getIsActive()).isFalse();
    }
}
