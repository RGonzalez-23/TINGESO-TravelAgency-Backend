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
    
    // To validate multi-package or frequent client (see status of a client's reservations)
    List<ReservationEntity> findByKeycloakUserIdAndStatus(String keycloakUserId, ReservationStatusEnum status);
    
    // To search for multi-package reservations in a specific time window
    List<ReservationEntity> findByKeycloakUserIdAndStatusAndPaidAtAfter(String keycloakUserId, ReservationStatusEnum status, LocalDateTime afterDate);
}
