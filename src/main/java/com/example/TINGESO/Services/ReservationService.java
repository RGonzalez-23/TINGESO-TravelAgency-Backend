package com.example.TINGESO.Services;

import com.example.TINGESO.DTOs.DiscountContextDTO;
import com.example.TINGESO.DTOs.PassengerDTO;
import com.example.TINGESO.DTOs.PaymentReceiptDTO;
import com.example.TINGESO.DTOs.PaymentRequestDTO;
import com.example.TINGESO.DTOs.ReservationRequestDTO;
import com.example.TINGESO.DTOs.ReservationResponseDTO;
import com.example.TINGESO.Entities.*;
import com.example.TINGESO.Repositories.ReservationRepository;
import com.example.TINGESO.Repositories.TourPackageRepository;
import com.example.TINGESO.Repositories.PaymentTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TourPackageRepository tourPackageRepository;

    @Autowired
    private PaymentTransactionRepository paymentRepository;

    @Autowired
    private DiscountEngineService discountEngineService;

    @Autowired
    private TaskScheduler taskScheduler; // Inyectamos el planificador de Spring Boot para timeout

    // Crear la reserva (Retorna DTO de respuesta para el frontend)
    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO req, String keycloakUserId) {
        if (req.getPassengersCount() == null || req.getPassengersCount() <= 0) {
            throw new RuntimeException("La cantidad de pasajeros debe ser mayor a 0");
        }

        TourPackageEntity tourPackage = tourPackageRepository.findById(req.getTourPackageId())
                .orElseThrow(() -> new RuntimeException("Paquete no encontrado"));

        if (tourPackage.getStatus() == PackageStatusEnum.AGOTADO ||
            tourPackage.getStatus() == PackageStatusEnum.CANCELADO ||
            tourPackage.getStatus() == PackageStatusEnum.NO_VIGENTE) {
            throw new RuntimeException("El paquete no está disponible para reserva (" + tourPackage.getStatus().name() + ")");
        }

        if (tourPackage.getAvailableSlots() < req.getPassengersCount()) {
            throw new RuntimeException("No hay cupos suficientes. Cupos disponibles: " + tourPackage.getAvailableSlots());
        }

        // Calcular montos y descuentos
        Double baseTotal = tourPackage.getPrice() * req.getPassengersCount();
        DiscountContextDTO discountCtx = discountEngineService.calculateDiscounts(keycloakUserId, req.getPassengersCount());
        
        Double discountAmount = baseTotal * (discountCtx.getFinalDiscountPercentage() / 100.0);
        Double finalAmount = baseTotal - discountAmount;
        
        if (finalAmount < 0) {
            finalAmount = 0.0;
        }

        // Descontar cupos del paquete
        tourPackage.setAvailableSlots(tourPackage.getAvailableSlots() - req.getPassengersCount());
        if (tourPackage.getAvailableSlots() == 0) {
            tourPackage.setStatus(PackageStatusEnum.AGOTADO);
        }
        tourPackageRepository.save(tourPackage);

        // Crear Entidad Reserva
        ReservationEntity res = new ReservationEntity();
        res.setKeycloakUserId(keycloakUserId);
        res.setTourPackage(tourPackage);
        res.setPassengersCount(req.getPassengersCount());
        res.setTotalAmount(baseTotal);
        res.setFinalAmount(finalAmount);
        res.setDiscountPercentage(discountCtx.getFinalDiscountPercentage());
        res.setAppliedDiscountsDetails(discountCtx.getAppliedDiscountsDetails());
        res.setStatus(ReservationStatusEnum.PENDIENTE);
        res.setSpecialRequests(req.getSpecialRequests());
        res.setPreferences(req.getPreferences());

        // Mapear pasajeros
        if (req.getPassengers() != null) {
            for (PassengerDTO pdto : req.getPassengers()) {
                PassengerEntity pass = new PassengerEntity();
                pass.setFullName(pdto.getFullName());
                pass.setAge(pdto.getAge());
                pass.setNeedsAssistance(pdto.getNeedsAssistance() != null ? pdto.getNeedsAssistance() : false);
                pass.setAssistanceDetails(pdto.getAssistanceDetails());
                pass.setReservation(res);
                res.getPassengers().add(pass);
            }
        }

        ReservationEntity savedRes = reservationRepository.save(res);

        // Programar la Tarea de Auto-Expiración en 3 minutos exactos (180000 ms)
        Instant expirationTime = Instant.now().plusMillis(180000);
        taskScheduler.schedule(() -> cancelReservationIfPending(savedRes.getId()), expirationTime);

        return mapToResponseDTO(savedRes);
    }

    // Tarea Programada que se ejecutará 3 minutos en el futuro
    @Transactional
    public void cancelReservationIfPending(Long reservationId) {
        ReservationEntity res = reservationRepository.findById(reservationId).orElse(null);
        if (res != null && res.getStatus() == ReservationStatusEnum.PENDIENTE) {
            res.setStatus(ReservationStatusEnum.CANCELADA);
            
            // Restituir cupos a la agencia
            TourPackageEntity tourPackage = res.getTourPackage();
            tourPackage.setAvailableSlots(tourPackage.getAvailableSlots() + res.getPassengersCount());
            
            // Si la agencia había quedado Agotada por esta reserva, vuelve a estar Disponible
            if (tourPackage.getStatus() == PackageStatusEnum.AGOTADO && tourPackage.getAvailableSlots() > 0) {
                tourPackage.setStatus(PackageStatusEnum.DISPONIBLE);
            }
            
            tourPackageRepository.save(tourPackage);
            reservationRepository.save(res);
            System.out.println("LOG: Reserva " + reservationId + " expiro por Timeout (1 minuto sin pagar). Cupos restaurados.");
        }
    }

    // Pay Reservation (Simulated Gateway)
    @Transactional
    public PaymentReceiptDTO payReservation(Long id, PaymentRequestDTO req, String keycloakUserId) {
        ReservationEntity res = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
                
        if (!res.getKeycloakUserId().equals(keycloakUserId)) {
            throw new RuntimeException("Unauthorized: You do not own this reservation");
        }

        if (res.getStatus() == ReservationStatusEnum.CANCELADA) {
            throw new RuntimeException("Cannot pay for a CANCELLED reservation");
        }

        if (res.getStatus() != ReservationStatusEnum.PENDIENTE) {
            throw new RuntimeException("Reservation is not PENDING. It might have already been paid.");
        }
        
        if (res.getFinalAmount() <= 0) {
            throw new RuntimeException("Payment amount must be greater than 0");
        }

        // Validate basic mock gateway inputs
        if (req.getCardNumber() == null || req.getCardNumber().length() != 16) {
            throw new RuntimeException("Invalid Card Number format (must be 16 digits)");
        }
        if (req.getCvv() == null || req.getCvv().length() != 3) {
            throw new RuntimeException("Invalid CVV format (must be 3 digits)");
        }

        // Follow Epic 5 constraints: We NEVER persist the sensitive credit card details.
        // Create the transaction record.
        PaymentTransactionEntity transaction = new PaymentTransactionEntity();
        transaction.setTransactionHash(UUID.randomUUID().toString().replace("-", "").toUpperCase());
        transaction.setAmountPaid(res.getFinalAmount());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setPaymentMethod(req.getPaymentMethod() != null ? req.getPaymentMethod() : "CREDIT_CARD");
        transaction.setReservation(res);

        paymentRepository.save(transaction);

        res.setStatus(ReservationStatusEnum.PAGADA);
        res.setPaidAt(transaction.getTransactionDate());
        reservationRepository.save(res);
        
        // Return a clean voucher representation to the frontend
        PaymentReceiptDTO receipt = new PaymentReceiptDTO();
        receipt.setId(transaction.getId());
        receipt.setTransactionHash(transaction.getTransactionHash());
        receipt.setAmountPaid(transaction.getAmountPaid());
        receipt.setTransactionDate(transaction.getTransactionDate());
        receipt.setPaymentMethod(transaction.getPaymentMethod());

        return receipt;
    }
    
    // Obtener mis Reservas
    public List<ReservationResponseDTO> getMyReservations(String keycloakUserId) {
        List<ReservationEntity> list = reservationRepository.findByKeycloakUserId(keycloakUserId);
        List<ReservationResponseDTO> dtoList = new ArrayList<>();
        for (ReservationEntity r : list) {
            dtoList.add(mapToResponseDTO(r));
        }
        return dtoList;
    }

    // Get All Reservations (For Admin)
    public List<ReservationResponseDTO> getAllReservations() {
        List<ReservationEntity> list = reservationRepository.findAll();
        List<ReservationResponseDTO> dtoList = new ArrayList<>();
        for (ReservationEntity r : list) {
            dtoList.add(mapToResponseDTO(r));
        }
        return dtoList;
    }

    // Update Reservation Status (Admin or Client confirming)
    @Transactional
    public ReservationResponseDTO updateReservationStatus(Long id, String newStatus, String keycloakUserId, boolean isAdmin) {
        ReservationEntity res = reservationRepository.findById(id).orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        
        ReservationStatusEnum statusEnum;
        try {
            statusEnum = ReservationStatusEnum.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado inválido");
        }

        if (res.getStatus() == ReservationStatusEnum.CANCELADA && !isAdmin) {
            throw new RuntimeException("La reserva ya está CANCELADA y no puede ser modificada por el cliente");
        }

        if (!isAdmin) {
            if (!res.getKeycloakUserId().equals(keycloakUserId)) {
                throw new RuntimeException("No tienes permiso sobre esta reserva");
            }
            if (statusEnum == ReservationStatusEnum.CONFIRMADA && res.getStatus() != ReservationStatusEnum.PAGADA) {
                throw new RuntimeException("Solo puedes CONFIRMAR una reserva que ya se encuentra PAGADA");
            }
        } else {
            // Si el ADMIN cancela forzadamente una reserva que no estaba cancelada, reintegramos cupos
            if (statusEnum == ReservationStatusEnum.CANCELADA && res.getStatus() != ReservationStatusEnum.CANCELADA) {
                TourPackageEntity tourPackage = res.getTourPackage();
                tourPackage.setAvailableSlots(tourPackage.getAvailableSlots() + res.getPassengersCount());
                if (tourPackage.getStatus() == PackageStatusEnum.AGOTADO && tourPackage.getAvailableSlots() > 0) {
                    tourPackage.setStatus(PackageStatusEnum.DISPONIBLE);
                }
                tourPackageRepository.save(tourPackage);
            }
        }

        res.setStatus(statusEnum);
        return mapToResponseDTO(reservationRepository.save(res));
    }

    private ReservationResponseDTO mapToResponseDTO(ReservationEntity r) {
        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(r.getId());
        dto.setPackageId(r.getTourPackage().getId());
        dto.setPackageName(r.getTourPackage().getName());
        dto.setDestination(r.getTourPackage().getDestination());
        dto.setStartDate(r.getTourPackage().getStartDate());
        dto.setEndDate(r.getTourPackage().getEndDate());
        dto.setKeycloakUserId(r.getKeycloakUserId());
        dto.setPassengersCount(r.getPassengersCount());
        dto.setTotalAmount(r.getTotalAmount());
        dto.setFinalAmount(r.getFinalAmount());
        dto.setDiscountPercentage(r.getDiscountPercentage());
        dto.setAppliedDiscountsDetails(r.getAppliedDiscountsDetails());
        dto.setStatus(r.getStatus());
        dto.setCreatedAt(r.getCreatedAt());
        
        List<PassengerDTO> plist = new ArrayList<>();
        if (r.getPassengers() != null) {
            for (PassengerEntity p : r.getPassengers()) {
                PassengerDTO pdto = new PassengerDTO();
                pdto.setFullName(p.getFullName());
                pdto.setAge(p.getAge());
                pdto.setNeedsAssistance(p.getNeedsAssistance());
                pdto.setAssistanceDetails(p.getAssistanceDetails());
                plist.add(pdto);
            }
        }
        dto.setPassengers(plist);
        
        return dto;
    }
}
