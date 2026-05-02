package com.example.TINGESO.Services;

import com.example.TINGESO.DTOs.DiscountContextDTO;
import com.example.TINGESO.Entities.*;
import com.example.TINGESO.Repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
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
    void initDefaultConfig_WhenEmpty() {
        when(configRepository.count()).thenReturn(0L);
        discountEngineService.initDefaultConfig();
        verify(configRepository, times(1)).save(any(GlobalDiscountConfigEntity.class));
    }

    @Test
    void calculateDiscounts_NoDiscountsApply() {
        GlobalDiscountConfigEntity config = new GlobalDiscountConfigEntity();
        config.setGroupMinPassengers(10);
        when(configRepository.findById(1L)).thenReturn(Optional.of(config));

        DiscountContextDTO ctx = discountEngineService.calculateDiscounts("user", 1);
        assertThat(ctx.getFinalDiscountPercentage()).isEqualTo(0.0);
    }

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

    @Test
    void calculateDiscounts_NonAccumulative_PicksHighest() {
        GlobalDiscountConfigEntity config = new GlobalDiscountConfigEntity();
        config.setGroupMinPassengers(2);
        config.setGroupDiscountPercentage(10.0); // 10%
        config.setAreDiscountsAccumulative(false);
        config.setMaxGlobalDiscountPercentageCap(100.0);
        
        when(configRepository.findById(1L)).thenReturn(Optional.of(config));

        PromotionEntity promo = new PromotionEntity();
        promo.setName("Mega Promo");
        promo.setDiscountPercentage(25.0); // 25%
        when(promotionRepository.findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(any(), any()))
                .thenReturn(Collections.singletonList(promo));

        DiscountContextDTO ctx = discountEngineService.calculateDiscounts("user", 5);
        
        // Debería elegir 25% y descartar el 10%
        assertThat(ctx.getFinalDiscountPercentage()).isEqualTo(25.0);
        assertThat(ctx.getAppliedDiscountsDetails()).contains("Excluyente");
    }

    @Test
    void calculateDiscounts_FrequentClientApplies() {
        GlobalDiscountConfigEntity config = new GlobalDiscountConfigEntity();
        config.setFrequentClientMinReservations(3);
        config.setFrequentClientDiscountPercentage(15.0);
        config.setAreDiscountsAccumulative(true);
        config.setMaxGlobalDiscountPercentageCap(50.0);
        
        when(configRepository.findById(1L)).thenReturn(Optional.of(config));
        when(reservationRepository.findByKeycloakUserIdAndStatus("user", ReservationStatusEnum.PAGADA))
            .thenReturn(java.util.Collections.nCopies(2, new ReservationEntity()));
        when(reservationRepository.findByKeycloakUserIdAndStatus("user", ReservationStatusEnum.CONFIRMADA))
            .thenReturn(java.util.Collections.nCopies(1, new ReservationEntity()));
            
        DiscountContextDTO ctx = discountEngineService.calculateDiscounts("user", 1);
        assertThat(ctx.getFinalDiscountPercentage()).isEqualTo(15.0);
    }

    @Test
    void calculateDiscounts_MultiPackageApplies() {
        GlobalDiscountConfigEntity config = new GlobalDiscountConfigEntity();
        config.setMultiPackageDaysWindow(7);
        config.setMultiPackageDiscountPercentage(5.0);
        config.setAreDiscountsAccumulative(true);
        config.setMaxGlobalDiscountPercentageCap(50.0);
        
        when(configRepository.findById(1L)).thenReturn(Optional.of(config));
        when(reservationRepository.findByKeycloakUserIdAndStatusAndPaidAtAfter(eq("user"), eq(ReservationStatusEnum.PAGADA), any()))
            .thenReturn(java.util.Collections.singletonList(new ReservationEntity()));
            
        DiscountContextDTO ctx = discountEngineService.calculateDiscounts("user", 1);
        assertThat(ctx.getFinalDiscountPercentage()).isEqualTo(5.0);
    }
}
