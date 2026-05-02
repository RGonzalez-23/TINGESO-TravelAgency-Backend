package com.example.TINGESO.Services;

import com.example.TINGESO.DTOs.DiscountContextDTO;
import com.example.TINGESO.Entities.GlobalDiscountConfigEntity;
import com.example.TINGESO.Repositories.GlobalDiscountConfigRepository;
import com.example.TINGESO.Repositories.PromotionRepository;
import com.example.TINGESO.Repositories.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountEngineServiceTest {

    @Mock private GlobalDiscountConfigRepository configRepository;
    @Mock private PromotionRepository promotionRepository;
    @Mock private ReservationRepository reservationRepository;

    @InjectMocks
    private DiscountEngineService discountEngineService;

    @Test
    void calculateDiscounts_GroupDiscountApplies() {
        GlobalDiscountConfigEntity config = new GlobalDiscountConfigEntity();
        config.setGroupMinPassengers(4);
        config.setGroupDiscountPercentage(10.0);
        config.setAreDiscountsAccumulative(true);
        config.setMaxGlobalDiscountPercentageCap(50.0);
        
        when(configRepository.findById(1L)).thenReturn(Optional.of(config));
        
        DiscountContextDTO ctx = discountEngineService.calculateDiscounts("user", 5);
        
        assertThat(ctx.getFinalDiscountPercentage()).isEqualTo(10.0);
        assertThat(ctx.getAppliedDiscountsDetails()).contains("Descuento Grupal");
    }

    @Test
    void calculateDiscounts_ExceedsCap_AppliesCap() {
        GlobalDiscountConfigEntity config = new GlobalDiscountConfigEntity();
        config.setGroupMinPassengers(2);
        config.setGroupDiscountPercentage(60.0);
        config.setAreDiscountsAccumulative(true);
        config.setMaxGlobalDiscountPercentageCap(35.0);
        
        when(configRepository.findById(1L)).thenReturn(Optional.of(config));
        
        DiscountContextDTO ctx = discountEngineService.calculateDiscounts("user", 5);
        
        assertThat(ctx.getFinalDiscountPercentage()).isEqualTo(35.0);
    }
}
