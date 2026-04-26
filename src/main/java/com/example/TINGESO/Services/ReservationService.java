package com.example.TINGESO.Services;

import com.example.TINGESO.DTOs.DiscountContextDTO;
import com.example.TINGESO.DTOs.PassengerDTO;
import com.example.TINGESO.DTOs.ReservationRequestDTO;
import com.example.TINGESO.DTOs.ReservationResponseDTO;
import com.example.TINGESO.Entities.*;
import com.example.TINGESO.Repositories.ReservationRepository;
import com.example.TINGESO.Repositories.TourPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TourPackageRepository tourPackageRepository;

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

    // Pagar Reserva
    @Transactional
    public ReservationResponseDTO payReservation(Long id, String keycloakUserId) {
        ReservationEntity res = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
                
        if (!res.getKeycloakUserId().equals(keycloakUserId)) {
            throw new RuntimeException("No tienes permiso sobre esta reserva");
        }

        if (res.getStatus() != ReservationStatusEnum.PENDIENTE) {
            throw new RuntimeException("La reserva no está en estado PENDIENTE");
        }
        
        res.setStatus(ReservationStatusEnum.PAGADA);
        res.setPaidAt(LocalDateTime.now());
        
        return mapToResponseDTO(reservationRepository.save(res));
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

    private ReservationResponseDTO mapToResponseDTO(ReservationEntity r) {
        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(r.getId());
        dto.setPackageId(r.getTourPackage().getId());
        dto.setPackageName(r.getTourPackage().getName());
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
