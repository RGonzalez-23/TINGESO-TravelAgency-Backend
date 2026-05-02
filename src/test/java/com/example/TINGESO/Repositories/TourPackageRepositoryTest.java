package com.example.TINGESO.Repositories;

import com.example.TINGESO.Entities.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

@DataJpaTest
class TourPackageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TourPackageRepository tourPackageRepository;

    @Test
    public void whenFindById_thenReturnTourPackage() {
        TourPackageEntity pkg = new TourPackageEntity();
        pkg.setName("Test Package");
        pkg.setDestination("Chile");
        pkg.setDescription("Desc");
        pkg.setStartDate(LocalDateTime.now().plusDays(1));
        pkg.setEndDate(LocalDateTime.now().plusDays(5));
        pkg.setDurationDays(4);
        pkg.setPrice(1000.0);
        pkg.setTotalSlots(10);
        pkg.setAvailableSlots(10);
        pkg.setTripType(TripTypeEnum.NACIONAL);
        pkg.setSeason(SeasonEnum.ALTA);
        pkg.setCategory(CategoryEnum.AVENTURA);
        pkg.setStatus(PackageStatusEnum.DISPONIBLE);
        pkg.setIsVisible(true);

        entityManager.persistAndFlush(pkg);

        Optional<TourPackageEntity> found = tourPackageRepository.findById(pkg.getId());

        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getName()).isEqualTo("Test Package");
    }
}
