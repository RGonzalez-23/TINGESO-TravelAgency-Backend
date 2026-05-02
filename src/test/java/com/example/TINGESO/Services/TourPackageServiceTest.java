package com.example.TINGESO.Services;

import com.example.TINGESO.DTOs.TourPackageRequest;
import com.example.TINGESO.Entities.TourPackageEntity;
import com.example.TINGESO.Repositories.TourPackageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
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
    void createPackage_Success() {
        TourPackageRequest req = new TourPackageRequest();
        req.setStartDate(LocalDateTime.now().plusDays(1));
        req.setEndDate(LocalDateTime.now().plusDays(5));
        req.setTotalSlots(10);
        req.setPrice(100.0);
        
        TourPackageEntity pkg = new TourPackageEntity();
        pkg.setAvailableSlots(10);
        
        when(tourPackageRepository.save(any(TourPackageEntity.class))).thenReturn(pkg);

        TourPackageEntity result = tourPackageService.createPackage(req);
        assertThat(result.getAvailableSlots()).isEqualTo(10); // Service should set availableSlots = totalSlots
    }

    @Test
    void updatePackage_NotFound_ThrowsException() {
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> tourPackageService.updatePackage(1L, new TourPackageRequest()));
    }
}
