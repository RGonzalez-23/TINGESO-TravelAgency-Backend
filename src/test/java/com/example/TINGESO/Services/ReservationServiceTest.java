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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
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
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(savedRes);

        ReservationResponseDTO result = reservationService.createReservation(req, "user1");
        
        assertThat(result).isNotNull();
        verify(tourPackageRepository).save(pkg); // Deducts slots
        verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class)); // Verifies auto-cancellation
    }

    @Test
    void createReservation_NoSlots_ThrowsException() {
        ReservationRequestDTO req = new ReservationRequestDTO();
        req.setTourPackageId(1L);
        req.setPassengersCount(6); // Asks for 6

        TourPackageEntity pkg = new TourPackageEntity();
        pkg.setId(1L);
        pkg.setAvailableSlots(5); // Only 5 available
        pkg.setStatus(PackageStatusEnum.DISPONIBLE);

        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));

        assertThrows(RuntimeException.class, () -> reservationService.createReservation(req, "user1"));
    }

    @Test
    void createReservation_PackageAgotado_ThrowsException() {
        ReservationRequestDTO req = new ReservationRequestDTO();
        req.setTourPackageId(1L);
        req.setPassengersCount(2);

        TourPackageEntity pkg = new TourPackageEntity();
        pkg.setId(1L);
        pkg.setAvailableSlots(5); 
        pkg.setStatus(PackageStatusEnum.AGOTADO); // Invalid Status

        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));

        assertThrows(RuntimeException.class, () -> reservationService.createReservation(req, "user1"));
    }

    @Test
    void cancelReservationIfPending_CancelsAndRestoresSlots() {
        ReservationEntity res = new ReservationEntity();
        res.setStatus(ReservationStatusEnum.PENDIENTE);
        res.setPassengersCount(2);

        TourPackageEntity pkg = new TourPackageEntity();
        pkg.setAvailableSlots(0);
        pkg.setStatus(PackageStatusEnum.AGOTADO);
        res.setTourPackage(pkg);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));

        reservationService.cancelReservationIfPending(1L);

        assertThat(res.getStatus()).isEqualTo(ReservationStatusEnum.CANCELADA);
        assertThat(pkg.getAvailableSlots()).isEqualTo(2);
        assertThat(pkg.getStatus()).isEqualTo(PackageStatusEnum.DISPONIBLE);
        verify(reservationRepository).save(res);
        verify(tourPackageRepository).save(pkg);
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

    @Test
    void payReservation_UnauthorizedUser_ThrowsException() {
        ReservationEntity res = new ReservationEntity();
        res.setKeycloakUserId("user1"); // Owner
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));

        PaymentRequestDTO payReq = new PaymentRequestDTO();

        assertThrows(RuntimeException.class, () -> reservationService.payReservation(1L, payReq, "hacker"));
    }

    @Test
    void payReservation_InvalidCard_ThrowsException() {
        ReservationEntity res = new ReservationEntity();
        res.setKeycloakUserId("user1");
        res.setStatus(ReservationStatusEnum.PENDIENTE);
        res.setFinalAmount(100.0);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));

        PaymentRequestDTO payReq = new PaymentRequestDTO();
        payReq.setCardNumber("123"); // Short card
        payReq.setCvv("123");

        assertThrows(RuntimeException.class, () -> reservationService.payReservation(1L, payReq, "user1"));
    }

    @Test
    void updateReservationStatus_AdminCancels_RestoresSlots() {
        ReservationEntity res = new ReservationEntity();
        res.setStatus(ReservationStatusEnum.PAGADA);
        res.setPassengersCount(2);

        TourPackageEntity pkg = new TourPackageEntity();
        pkg.setAvailableSlots(0);
        pkg.setStatus(PackageStatusEnum.AGOTADO);
        res.setTourPackage(pkg);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(res);

        ReservationResponseDTO dto = reservationService.updateReservationStatus(1L, "CANCELADA", "admin", true);
        
        assertThat(dto.getStatus()).isEqualTo(ReservationStatusEnum.CANCELADA);
        assertThat(pkg.getAvailableSlots()).isEqualTo(2); // Restored
        verify(tourPackageRepository).save(pkg);
    }
    
    @Test
    void updateReservationStatus_ClientTriesToConfirmWithoutPaying_ThrowsException() {
        ReservationEntity res = new ReservationEntity();
        res.setStatus(ReservationStatusEnum.PENDIENTE); // Not paid
        res.setKeycloakUserId("user1");
        
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));

        assertThrows(RuntimeException.class, () -> reservationService.updateReservationStatus(1L, "CONFIRMADA", "user1", false));
    }

    @Test
    void getMyReservations_ReturnsList() {
        ReservationEntity res = new ReservationEntity();
        res.setId(1L);
        res.setKeycloakUserId("user1");
        TourPackageEntity pkg = new TourPackageEntity();
        pkg.setId(1L);
        pkg.setName("Test");
        res.setTourPackage(pkg);

        when(reservationRepository.findByKeycloakUserId("user1")).thenReturn(java.util.Collections.singletonList(res));

        List<ReservationResponseDTO> result = reservationService.getMyReservations("user1");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKeycloakUserId()).isEqualTo("user1");
    }

    @Test
    void getAllReservations_ReturnsList() {
        ReservationEntity res = new ReservationEntity();
        res.setId(1L);
        TourPackageEntity pkg = new TourPackageEntity();
        pkg.setId(1L);
        pkg.setName("Test");
        res.setTourPackage(pkg);

        when(reservationRepository.findAll()).thenReturn(java.util.Collections.singletonList(res));

        List<ReservationResponseDTO> result = reservationService.getAllReservations();
        assertThat(result).hasSize(1);
    }

    @Test
    void updateReservationStatus_ClientCannotCancelIfAlreadyCancelled() {
        ReservationEntity res = new ReservationEntity();
        res.setId(1L);
        res.setStatus(ReservationStatusEnum.CANCELADA);
        
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));

        assertThrows(RuntimeException.class, () -> reservationService.updateReservationStatus(1L, "CANCELADA", "user1", false));
    }

    @Test
    void updateReservationStatus_AdminCanCancelAnytime() {
        ReservationEntity res = new ReservationEntity();
        res.setId(1L);
        res.setStatus(ReservationStatusEnum.PENDIENTE);
        res.setPassengersCount(2);

        TourPackageEntity pkg = new TourPackageEntity();
        pkg.setAvailableSlots(5);
        res.setTourPackage(pkg);
        
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));
        when(reservationRepository.save(any())).thenReturn(res);

        ReservationResponseDTO dto = reservationService.updateReservationStatus(1L, "CANCELADA", "admin", true);
        assertThat(dto.getStatus()).isEqualTo(ReservationStatusEnum.CANCELADA);
    }

    @Test
    void createReservation_ZeroPassengers_ThrowsException() {
        ReservationRequestDTO req = new ReservationRequestDTO();
        req.setPassengersCount(0);
        assertThrows(RuntimeException.class, () -> reservationService.createReservation(req, "user1"));
    }

    @Test
    void createReservation_WithPassengersList_Success() {
        ReservationRequestDTO req = new ReservationRequestDTO();
        req.setTourPackageId(1L);
        req.setPassengersCount(1);
        PassengerDTO pass = new PassengerDTO();
        pass.setFullName("John Doe");
        req.setPassengers(java.util.Collections.singletonList(pass));

        TourPackageEntity pkg = new TourPackageEntity();
        pkg.setId(1L);
        pkg.setPrice(100.0);
        pkg.setAvailableSlots(5);
        pkg.setStatus(PackageStatusEnum.DISPONIBLE);

        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        when(discountEngineService.calculateDiscounts(any(), anyInt())).thenReturn(new DiscountContextDTO());
        
        ReservationEntity savedRes = new ReservationEntity();
        savedRes.setId(1L);
        savedRes.setTourPackage(pkg);
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(savedRes);

        ReservationResponseDTO result = reservationService.createReservation(req, "user1");
        assertThat(result).isNotNull();
    }

    @Test
    void payReservation_ZeroAmount_ThrowsException() {
        ReservationEntity res = new ReservationEntity();
        res.setKeycloakUserId("user1");
        res.setStatus(ReservationStatusEnum.PENDIENTE);
        res.setFinalAmount(0.0); // Invalid to pay 0 amount through gateway
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));

        assertThrows(RuntimeException.class, () -> reservationService.payReservation(1L, new PaymentRequestDTO(), "user1"));
    }

    @Test
    void payReservation_CancelledReservation_ThrowsException() {
        ReservationEntity res = new ReservationEntity();
        res.setKeycloakUserId("user1");
        res.setStatus(ReservationStatusEnum.CANCELADA);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));

        assertThrows(RuntimeException.class, () -> reservationService.payReservation(1L, new PaymentRequestDTO(), "user1"));
    }

    @Test
    void updateReservationStatus_InvalidStatus_ThrowsException() {
        ReservationEntity res = new ReservationEntity();
        res.setId(1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));

        assertThrows(RuntimeException.class, () -> reservationService.updateReservationStatus(1L, "INVALIDO", "admin", true));
    }

    @Test
    void updateReservationStatus_ClientEditsOtherUserReservation_ThrowsException() {
        ReservationEntity res = new ReservationEntity();
        res.setId(1L);
        res.setKeycloakUserId("user1");
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));

        assertThrows(RuntimeException.class, () -> reservationService.updateReservationStatus(1L, "CONFIRMADA", "hacker", false));
    }
}
