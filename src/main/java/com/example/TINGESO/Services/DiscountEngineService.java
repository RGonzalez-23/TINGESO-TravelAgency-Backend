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
        // En caso de que no haya configuración cargada en DB, instanciamos la default en memoria temporalmente
        GlobalDiscountConfigEntity config = configRepository.findById(1L).orElseGet(GlobalDiscountConfigEntity::new);
        
        List<String> discountReasons = new ArrayList<>();
        List<Double> applicableDiscounts = new ArrayList<>();

        // 1. Descuento Grupal
        if (passengersCount >= config.getGroupMinPassengers()) {
            applicableDiscounts.add(config.getGroupDiscountPercentage());
            discountReasons.add("Descuento Grupal (" + config.getGroupDiscountPercentage() + "%)");
        }

        // 2. Cliente Frecuente (Reservas pagadas previas)
        int paidReservations = reservationRepository.findByKeycloakUserIdAndStatus(keycloakUserId, ReservationStatusEnum.PAGADA).size();
        if (paidReservations >= config.getFrequentClientMinReservations()) {
            applicableDiscounts.add(config.getFrequentClientDiscountPercentage());
            discountReasons.add("Cliente Frecuente (" + config.getFrequentClientDiscountPercentage() + "%)");
        }

        // 3. Múltiples Paquetes (Reservas pagadas recientemente dentro de una ventana de tiempo)
        LocalDateTime windowStart = LocalDateTime.now().minusDays(config.getMultiPackageDaysWindow());
        int recentPaidReservations = reservationRepository.findByKeycloakUserIdAndStatusAndPaidAtAfter(
                keycloakUserId, ReservationStatusEnum.PAGADA, windowStart).size();
        
        if (recentPaidReservations > 0) {
            applicableDiscounts.add(config.getMultiPackageDiscountPercentage());
            discountReasons.add("Compra Múltiple Reciente (" + config.getMultiPackageDiscountPercentage() + "%)");
        }

        // 4. Promociones Temporales Activas (Ej. Black Friday)
        List<PromotionEntity> activePromotions = promotionRepository.findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(
                LocalDateTime.now(), LocalDateTime.now());
        
        for (PromotionEntity promo : activePromotions) {
            applicableDiscounts.add(promo.getDiscountPercentage());
            discountReasons.add("Promo: " + promo.getName() + " (" + promo.getDiscountPercentage() + "%)");
        }

        // Calcular % Total de descuento a aplicar
        double finalPercentage = 0.0;
        
        if (!applicableDiscounts.isEmpty()) {
            if (config.getAreDiscountsAccumulative()) {
                // Sumar todos los descuentos directamente
                finalPercentage = applicableDiscounts.stream().mapToDouble(Double::doubleValue).sum();
            } else {
                // Regla Excluyente: Seleccionar solo la oferta con el MAYOR descuento
                double max = applicableDiscounts.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                finalPercentage = max;
                // Sobrescribimos las razones para indicar que solo se aplicó el mayor
                discountReasons.clear();
                discountReasons.add("Descuento Base Excluyente (" + max + "%)");
            }
        }

        // Aplicar límite global (Cap máximo permitido de descuentos)
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
