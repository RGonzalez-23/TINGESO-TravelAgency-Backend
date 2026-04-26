package com.example.TINGESO.Repositories;

import com.example.TINGESO.Entities.ReservationEntity;
import com.example.TINGESO.Entities.ReservationStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    List<ReservationEntity> findByKeycloakUserId(String keycloakUserId);
    
    // Para validar multipaquete o cliente frecuente (ver estado de las reservas de un cliente)
    List<ReservationEntity> findByKeycloakUserIdAndStatus(String keycloakUserId, ReservationStatusEnum status);
    
    // Para buscar reservas de multipaquete en una ventana de tiempo específica
    List<ReservationEntity> findByKeycloakUserIdAndStatusAndPaidAtAfter(String keycloakUserId, ReservationStatusEnum status, LocalDateTime afterDate);
}
