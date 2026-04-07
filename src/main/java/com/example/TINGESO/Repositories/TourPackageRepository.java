package com.example.TINGESO.Repositories;

import com.example.TINGESO.Entities.TourPackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourPackageRepository extends JpaRepository<TourPackageEntity, Long> {
}
