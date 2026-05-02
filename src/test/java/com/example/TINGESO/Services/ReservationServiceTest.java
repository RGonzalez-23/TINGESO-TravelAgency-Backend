package com.example.TINGESO.Services;

import com.example.TINGESO.DTOs.*;
import com.example.TINGESO.Entities.*;
import com.example.TINGESO.Repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private TourPackageRepository tourPackageRepository;
    @Mock private PaymentTransactionRepository paymentRepository;
    @Mock private DiscountEngineService discountEngineService;
    @Mock private TaskScheduler taskScheduler;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void createReservation_Success() {
        ReservationRequestDTO req = new ReservationRequestDTO();
        req.setTourPackageId(1L);
        req.setPassengersCount(2);

        TourPackageEntity pkg = new TourPackageEntity();
        pkg.setId(1L);
        pkg.setPrice(100.0);
        pkg.setAvailableSlots(5);
        pkg.setStatus(PackageStatusEnum.DISPONIBLE);

        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));

        DiscountContextDTO discountCtx = new DiscountContextDTO();
        discountCtx.setFinalDiscountPercentage(10.0);
        when(discountEngineService.calculateDiscounts(any(), anyInt())).thenReturn(discountCtx);

        ReservationEntity savedRes = new ReservationEntity();
        savedRes.setId(1L);
        savedRes.setTourPackage(pkg);
        when(reservationRepository.save(any())).thenReturn(savedRes);

        ReservationResponseDTO result = reservationService.createReservation(req, "user1");
        
        assertThat(result).isNotNull();
        verify(tourPackageRepository).save(pkg); // Should deduct slots
    }

    @Test
    void payReservation_Success() {
        ReservationEntity res = new ReservationEntity();
        res.setKeycloakUserId("user1");
        res.setStatus(ReservationStatusEnum.PENDIENTE);
        res.setFinalAmount(100.0);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));

        PaymentRequestDTO payReq = new PaymentRequestDTO();
        payReq.setCardNumber("1234567812345678");
        payReq.setCvv("123");

        PaymentReceiptDTO receipt = reservationService.payReservation(1L, payReq, "user1");

        assertThat(receipt).isNotNull();
        assertThat(res.getStatus()).isEqualTo(ReservationStatusEnum.PAGADA);
        verify(paymentRepository).save(any());
    }
}
