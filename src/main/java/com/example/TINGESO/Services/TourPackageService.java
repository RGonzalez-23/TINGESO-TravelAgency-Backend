package com.example.TINGESO.Services;

import com.example.TINGESO.DTOs.TourPackageRequest;
import com.example.TINGESO.Entities.PackageStatusEnum;
import com.example.TINGESO.Entities.TourPackageEntity;
import com.example.TINGESO.Repositories.TourPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public TourPackageEntity getPackageById(Long id) {
        return tourPackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paquete no encontrado"));
    }

    public TourPackageEntity createPackage(TourPackageRequest req) {
        validateBusinessRules(req);

        TourPackageEntity entity = new TourPackageEntity();
        mapDtoToEntity(req, entity);
        
        // Valores por defecto al momento de Crear (Basado en el requerimiento)
        entity.setAvailableSlots(req.getTotalSlots());
        entity.setStatus(req.getStatus() != null ? req.getStatus() : PackageStatusEnum.DISPONIBLE);
        entity.setIsVisible(req.getIsVisible() != null ? req.getIsVisible() : true);

        return tourPackageRepository.save(entity);
    }

    public TourPackageEntity updatePackage(Long id, TourPackageRequest req) {
        TourPackageEntity entity = getPackageById(id);
                
        validateBusinessRules(req);
        
        // [Regla de Negocio Épica 2]: "Al modificar, NO debe afectar reservas realizadas (cupos totales menores al número ya reservado)"
        int occupiedSlots = entity.getTotalSlots() - entity.getAvailableSlots();
        if (req.getTotalSlots() < occupiedSlots) {
            throw new RuntimeException("Error: Los nuevos cupos totales (" + req.getTotalSlots() + ") no pueden ser menores a las reservas ya pagadas/realizadas (" + occupiedSlots + ").");
        }
        
        mapDtoToEntity(req, entity);
        
        // Recalcular cupos disponibles automáticamente
        entity.setAvailableSlots(req.getTotalSlots() - occupiedSlots);
        
        // [Regla de Negocio Épica 2]: "Si la disponibilidad es 0, NO se puede marcar como disponible"
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

    private void validateBusinessRules(TourPackageRequest req) {
        if (req.getStartDate() == null || req.getEndDate() == null) {
            throw new RuntimeException("Las fechas de inicio y término son obligatorias");
        }
        // [Regla de Negocio Épica 2]: "Fecha termino SI O SI posterior a fecha de inicio"
        if (!req.getEndDate().isAfter(req.getStartDate())) {
            throw new RuntimeException("Error: La fecha de término DEBE ser estrictamente posterior a la fecha de inicio");
        }
        // [Regla de Negocio Épica 2]: "Precio estrictamente mayor a 0"
        if (req.getPrice() == null || req.getPrice() <= 0) {
            throw new RuntimeException("Error: El precio base debe ser mayor a 0 CLP");
        }
        // [Regla de Negocio Épica 2]: "Cupos totales estrictamente mayor a 0"
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
        
        // Calculo automático de duración en días
        long days = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate());
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
