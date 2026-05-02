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
        TourPackageEntity existing = new TourPackageEntity();
        existing.setTotalSlots(10);
        existing.setAvailableSlots(8); // 2 occupied
        existing.setStatus(PackageStatusEnum.DISPONIBLE);
        
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(existing));

        TourPackageRequest req = new TourPackageRequest();
        req.setStartDate(LocalDateTime.now().plusDays(1));
        req.setEndDate(LocalDateTime.now().plusDays(5));
        req.setTotalSlots(15);
        req.setPrice(100.0);
        
        TourPackageEntity saved = new TourPackageEntity();
        saved.setAvailableSlots(13); // 15 - 2
        
        when(tourPackageRepository.save(any(TourPackageEntity.class))).thenReturn(saved);

        TourPackageEntity result = tourPackageService.updatePackage(1L, req);
        assertThat(result.getAvailableSlots()).isEqualTo(13);
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
    void deletePackage_Success() {
        doNothing().when(tourPackageRepository).deleteById(1L);
        tourPackageService.deletePackage(1L);
        verify(tourPackageRepository, times(1)).deleteById(1L);
    }
}
