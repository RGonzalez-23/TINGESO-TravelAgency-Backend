package com.example.TINGESO.Services;

import com.example.TINGESO.DTOs.DiscountContextDTO;
import com.example.TINGESO.Entities.GlobalDiscountConfigEntity;
import com.example.TINGESO.Entities.PromotionEntity;
import com.example.TINGESO.Entities.ReservationStatusEnum;
import com.example.TINGESO.Repositories.GlobalDiscountConfigRepository;
import com.example.TINGESO.Repositories.PromotionRepository;
import com.example.TINGESO.Repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DiscountEngineService {

    @Autowired
    private GlobalDiscountConfigRepository configRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @PostConstruct
    public void initDefaultConfig() {
        if (configRepository.count() == 0) {
            configRepository.save(new GlobalDiscountConfigEntity());
        }
    }

    public DiscountContextDTO calculateDiscounts(String keycloakUserId, int passengersCount) {
        // In case there is no configuration loaded in DB, we instantiate the default in memory temporarily
        GlobalDiscountConfigEntity config = configRepository.findById(1L).orElseGet(GlobalDiscountConfigEntity::new);
        
        List<String> discountReasons = new ArrayList<>();
        List<Double> applicableDiscounts = new ArrayList<>();

        // 1. Group Discount
        if (passengersCount >= config.getGroupMinPassengers()) {
            applicableDiscounts.add(config.getGroupDiscountPercentage());
            discountReasons.add("Descuento Grupal (" + config.getGroupDiscountPercentage() + "%)");
        }

        // 2. Frequent Client (Previous paid reservations)
        int paidReservations = reservationRepository.findByKeycloakUserIdAndStatus(keycloakUserId, ReservationStatusEnum.PAGADA).size();
        int confirmedReservations = reservationRepository.findByKeycloakUserIdAndStatus(keycloakUserId, ReservationStatusEnum.CONFIRMADA).size();
        if (paidReservations + confirmedReservations >= config.getFrequentClientMinReservations()) {
            applicableDiscounts.add(config.getFrequentClientDiscountPercentage());
            discountReasons.add("Cliente Frecuente (" + config.getFrequentClientDiscountPercentage() + "%)");
        }

        // 3. Multiple Packages (Recently paid reservations within a time window)
        LocalDateTime windowStart = LocalDateTime.now().minusDays(config.getMultiPackageDaysWindow());
        int recentPaidReservations = reservationRepository.findByKeycloakUserIdAndStatusAndPaidAtAfter(
                keycloakUserId, ReservationStatusEnum.PAGADA, windowStart).size();
        
        if (recentPaidReservations > 0) {
            applicableDiscounts.add(config.getMultiPackageDiscountPercentage());
            discountReasons.add("Compra Múltiple Reciente (" + config.getMultiPackageDiscountPercentage() + "%)");
        }

        // 4. Active Temporary Promotions (e.g., Black Friday)
        List<PromotionEntity> activePromotions = promotionRepository.findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(
                LocalDateTime.now(), LocalDateTime.now());
        
        for (PromotionEntity promo : activePromotions) {
            applicableDiscounts.add(promo.getDiscountPercentage());
            discountReasons.add("Promo: " + promo.getName() + " (" + promo.getDiscountPercentage() + "%)");
        }

        // Calculate Total % discount to apply
        double finalPercentage = 0.0;
        
        if (!applicableDiscounts.isEmpty()) {
            if (config.getAreDiscountsAccumulative()) {
                // Sum all discounts directly
                finalPercentage = applicableDiscounts.stream().mapToDouble(Double::doubleValue).sum();
            } else {
                // Exclusive Rule: Select only the offer with the HIGHEST discount
                double max = applicableDiscounts.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                finalPercentage = max;
                // We overwrite the reasons to indicate that only the highest was applied
                discountReasons.clear();
                discountReasons.add("Descuento Base Excluyente (" + max + "%)");
            }
        }

        // Apply global limit (Maximum allowed discount cap)
        if (finalPercentage > config.getMaxGlobalDiscountPercentageCap()) {
            finalPercentage = config.getMaxGlobalDiscountPercentageCap();
            discountReasons.add("[Límite Máximo del " + config.getMaxGlobalDiscountPercentageCap() + "% Alcanzado]");
        }

        DiscountContextDTO result = new DiscountContextDTO();
        result.setFinalDiscountPercentage(finalPercentage);
        result.setAppliedDiscountsDetails(String.join(", ", discountReasons));

        return result;
    }
}
