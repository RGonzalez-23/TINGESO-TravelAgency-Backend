package com.example.TINGESO.Services;

import com.example.TINGESO.DTOs.TourPackageFilterDTO;
import com.example.TINGESO.DTOs.TourPackageRequest;
import com.example.TINGESO.Entities.PackageStatusEnum;
import com.example.TINGESO.Entities.TourPackageEntity;
import com.example.TINGESO.Repositories.TourPackageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourPackageServiceTest {

    @Mock
    private TourPackageRepository tourPackageRepository;

    @InjectMocks
    private TourPackageService tourPackageService;

    @Test
    void getAllPackages_ShouldReturnList() {
        TourPackageEntity pkg = new TourPackageEntity();
        when(tourPackageRepository.findAll()).thenReturn(Collections.singletonList(pkg));

        List<TourPackageEntity> result = tourPackageService.getAllPackages();
        assertThat(result).hasSize(1);
    }

    @Test
    void searchPackages_ShouldReturnList() {
        TourPackageFilterDTO filter = new TourPackageFilterDTO();
        TourPackageEntity pkg = new TourPackageEntity();
        when(tourPackageRepository.findAll(any(Specification.class))).thenReturn(Collections.singletonList(pkg));

        List<TourPackageEntity> result = tourPackageService.searchPackages(filter);
        assertThat(result).hasSize(1);
    }

    @Test
    void getPackageById_Found() {
        TourPackageEntity pkg = new TourPackageEntity();
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        assertThat(tourPackageService.getPackageById(1L)).isNotNull();
    }

    @Test
    void getPackageById_NotFound() {
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> tourPackageService.getPackageById(1L));
    }

    @Test
    void createPackage_Success() {
        TourPackageRequest req = new TourPackageRequest();
        req.setStartDate(LocalDateTime.now().plusDays(1));
        req.setEndDate(LocalDateTime.now().plusDays(5));
        req.setTotalSlots(10);
        req.setPrice(100.0);
        
        TourPackageEntity savedPkg = new TourPackageEntity();
        savedPkg.setAvailableSlots(10);
        savedPkg.setStatus(PackageStatusEnum.DISPONIBLE);
        
        when(tourPackageRepository.save(any(TourPackageEntity.class))).thenReturn(savedPkg);

        TourPackageEntity result = tourPackageService.createPackage(req);
        assertThat(result.getAvailableSlots()).isEqualTo(10);
        assertThat(result.getStatus()).isEqualTo(PackageStatusEnum.DISPONIBLE);
    }

    @Test
    void createPackage_InvalidDates_ThrowsException() {
        TourPackageRequest req = new TourPackageRequest();
        req.setStartDate(LocalDateTime.now().plusDays(5));
        req.setEndDate(LocalDateTime.now().plusDays(1)); // Invalid
        
        assertThrows(RuntimeException.class, () -> tourPackageService.createPackage(req));
    }

    @Test
    void createPackage_InvalidPrice_ThrowsException() {
        TourPackageRequest req = new TourPackageRequest();
        req.setStartDate(LocalDateTime.now().plusDays(1));
        req.setEndDate(LocalDateTime.now().plusDays(5));
        req.setTotalSlots(10);
        req.setPrice(-50.0); // Invalid
        
        assertThrows(RuntimeException.class, () -> tourPackageService.createPackage(req));
    }

    @Test
    void updatePackage_Success() {
        // Define shared dates to ensure they match during the "date modification" validation
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(5);

        // Prepare the existing entity (Mocked database state)
        TourPackageEntity existing = new TourPackageEntity();
        existing.setTotalSlots(10);
        existing.setAvailableSlots(8); // 2 slots are already occupied (10 - 8 = 2)
        existing.setStatus(PackageStatusEnum.DISPONIBLE);

        // CRITICAL FIX: Set the dates on the existing entity to avoid NullPointerException
        // when the service calls .toLocalDate()
        existing.setStartDate(start);
        existing.setEndDate(end);

        // Mock the repository to return the "existing" entity when searched by ID
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(existing));

        // Prepare the update request (DTO)
        TourPackageRequest req = new TourPackageRequest();
        req.setStartDate(start); // Must match existing date because there are active reservations
        req.setEndDate(end);
        req.setTotalSlots(15);    // Increasing total slots
        req.setPrice(100.0);
        req.setName("Updated Package Name");

        // Prepare the entity that we expect to be saved
        TourPackageEntity saved = new TourPackageEntity();
        saved.setAvailableSlots(13); // Expected: 15 (new total) - 2 (already occupied) = 13

        // Mock the save operation to return our "saved" object
        when(tourPackageRepository.save(any(TourPackageEntity.class))).thenReturn(saved);

        // Execute the service method
        TourPackageEntity result = tourPackageService.updatePackage(1L, req);

        // Assertions to verify correct behavior and coverage
        assertThat(result.getAvailableSlots()).isEqualTo(13);
        verify(tourPackageRepository, times(1)).save(any(TourPackageEntity.class));
    }

    @Test
    void updatePackage_FailsWhenTotalSlotsLessThanOccupied() {
        TourPackageEntity existing = new TourPackageEntity();
        existing.setTotalSlots(10);
        existing.setAvailableSlots(5); // 5 occupied
        
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(existing));

        TourPackageRequest req = new TourPackageRequest();
        req.setStartDate(LocalDateTime.now().plusDays(1));
        req.setEndDate(LocalDateTime.now().plusDays(5));
        req.setTotalSlots(3); // Invalid, can't be less than 5
        req.setPrice(100.0);
        
        assertThrows(RuntimeException.class, () -> tourPackageService.updatePackage(1L, req));
    }

    @Test
    void updatePackage_FailsWhenForcingDisponibleWithZeroSlots() {
        TourPackageEntity existing = new TourPackageEntity();
        existing.setTotalSlots(10);
        existing.setAvailableSlots(0); // 10 occupied
        existing.setStatus(PackageStatusEnum.AGOTADO);

        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(existing));

        TourPackageRequest req = new TourPackageRequest();
        req.setStartDate(LocalDateTime.now().plusDays(1));
        req.setEndDate(LocalDateTime.now().plusDays(5));
        req.setTotalSlots(10); // Still 0 available
        req.setPrice(100.0);
        req.setStatus(PackageStatusEnum.DISPONIBLE); // Invalid, slots are 0

        assertThrows(RuntimeException.class, () -> tourPackageService.updatePackage(1L, req));
    }

    @Test
    void updatePackage_FailsWhenModifyingDatesWithExistingReservations() {
        TourPackageEntity existing = new TourPackageEntity();
        existing.setTotalSlots(10);
        existing.setAvailableSlots(8); // 2 occupied
        existing.setStartDate(java.time.LocalDateTime.now().plusDays(1));
        existing.setEndDate(java.time.LocalDateTime.now().plusDays(5));
        
        when(tourPackageRepository.findById(1L)).thenReturn(java.util.Optional.of(existing));

        TourPackageRequest req = new TourPackageRequest();
        req.setStartDate(java.time.LocalDateTime.now().plusDays(2)); // CHANGED DATE
        req.setEndDate(java.time.LocalDateTime.now().plusDays(5));
        req.setTotalSlots(15);
        req.setPrice(100.0);
        
        assertThrows(RuntimeException.class, () -> tourPackageService.updatePackage(1L, req));
    }

    @Test
    void deletePackage_Success() {
        doNothing().when(tourPackageRepository).deleteById(1L);
        tourPackageService.deletePackage(1L);
        verify(tourPackageRepository, times(1)).deleteById(1L);
    }
}
