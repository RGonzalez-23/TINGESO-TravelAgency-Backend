package com.example.TINGESO.Services;

import com.example.TINGESO.DTOs.TourPackageRequest;
import com.example.TINGESO.Entities.PackageStatusEnum;
import com.example.TINGESO.Entities.TourPackageEntity;
import com.example.TINGESO.Repositories.TourPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TourPackageService {

    @Autowired
    private TourPackageRepository tourPackageRepository;

    public List<TourPackageEntity> getAllPackages() {
        return tourPackageRepository.findAll();
    }

    // New method for Epic 3: Dynamic search using a DTO
    public List<TourPackageEntity> searchPackages(com.example.TINGESO.DTOs.TourPackageFilterDTO filterDTO) {
        return tourPackageRepository.findAll(filterDTO.toSpecification());
    }

    public TourPackageEntity getPackageById(Long id) {
        return tourPackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paquete no encontrado"));
    }

    public TourPackageEntity createPackage(TourPackageRequest req) {
        validateBusinessRules(req);

        TourPackageEntity entity = new TourPackageEntity();
        mapDtoToEntity(req, entity);
        
        // Default values at creation time (Based on the requirement)
        entity.setAvailableSlots(req.getTotalSlots());
        entity.setStatus(req.getStatus() != null ? req.getStatus() : PackageStatusEnum.DISPONIBLE);
        entity.setIsVisible(req.getIsVisible() != null ? req.getIsVisible() : true);

        return tourPackageRepository.save(entity);
    }

    public TourPackageEntity updatePackage(Long id, TourPackageRequest req) {
        TourPackageEntity entity = getPackageById(id);
                
        validateBusinessRules(req);
        
        // [Epic 2 Business Rule]: "When modifying, it MUST NOT affect made reservations (total slots less than the number already reserved)"
        int occupiedSlots = entity.getTotalSlots() - entity.getAvailableSlots();
        if (req.getTotalSlots() < occupiedSlots) {
            throw new RuntimeException("Error: Los nuevos cupos totales (" + req.getTotalSlots() + ") no pueden ser menores a las reservas ya pagadas/realizadas (" + occupiedSlots + ").");
        }
        
        // [Epic 2 Business Rule]: "If it has reservations, base dates CANNOT be modified"
        if (occupiedSlots > 0) {
            if (!entity.getStartDate().toLocalDate().equals(req.getStartDate().toLocalDate()) || 
                !entity.getEndDate().toLocalDate().equals(req.getEndDate().toLocalDate())) {
                throw new RuntimeException("Error: No puedes modificar las fechas base de un paquete que ya cuenta con reservas activas. Crea un paquete nuevo.");
            }
        }
        
        mapDtoToEntity(req, entity);
        
        // Recalculate available slots automatically
        entity.setAvailableSlots(req.getTotalSlots() - occupiedSlots);
        
        // [Epic 2 Business Rule]: "If availability is 0, it CANNOT be marked as available"
        PackageStatusEnum newStatus = req.getStatus() != null ? req.getStatus() : entity.getStatus();
        if (entity.getAvailableSlots() == 0 && newStatus == PackageStatusEnum.DISPONIBLE) {
            throw new RuntimeException("Error: Como los cupos disponibles son 0, el paquete NO se puede forzar al estado DISPONIBLE.");
        }
        entity.setStatus(newStatus);
        
        if (req.getIsVisible() != null) {
            entity.setIsVisible(req.getIsVisible());
        }

        return tourPackageRepository.save(entity);
    }

    public void deletePackage(Long id) {
        tourPackageRepository.deleteById(id);
    }

    private void validateBusinessRules(TourPackageRequest req) {
        if (req.getStartDate() == null || req.getEndDate() == null) {
            throw new RuntimeException("Las fechas de inicio y término son obligatorias");
        }
        // [Epic 2 Business Rule]: "End date MUST BE strictly after start date"
        if (!req.getEndDate().isAfter(req.getStartDate())) {
            throw new RuntimeException("Error: La fecha de término DEBE ser estrictamente posterior a la fecha de inicio");
        }
        // [Epic 2 Business Rule]: "Price strictly greater than 0"
        if (req.getPrice() == null || req.getPrice() <= 0) {
            throw new RuntimeException("Error: El precio base debe ser mayor a 0 CLP");
        }
        // [Epic 2 Business Rule]: "Total slots strictly greater than 0"
        if (req.getTotalSlots() == null || req.getTotalSlots() <= 0) {
            throw new RuntimeException("Error: Los cupos totales iniciales deben ser mayores a 0");
        }
    }

    private void mapDtoToEntity(TourPackageRequest req, TourPackageEntity entity) {
        entity.setName(req.getName());
        entity.setDestination(req.getDestination());
        entity.setDescription(req.getDescription());
        entity.setStartDate(req.getStartDate());
        entity.setEndDate(req.getEndDate());
        
        // Automatic calculation of duration in days
        long days = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;
        entity.setDurationDays((int) days);

        entity.setPrice(req.getPrice());
        entity.setTotalSlots(req.getTotalSlots());
        entity.setIncludedServices(req.getIncludedServices());
        entity.setConditions(req.getConditions());
        entity.setRestrictions(req.getRestrictions());
        entity.setTripType(req.getTripType());
        entity.setSeason(req.getSeason());
        entity.setCategory(req.getCategory());
    }
}
