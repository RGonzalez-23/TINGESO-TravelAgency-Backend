package com.example.TINGESO.Repositories;

import com.example.TINGESO.Entities.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
class ReservationRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ReservationRepository reservationRepository;

    private TourPackageEntity createMockPackage() {
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
        return entityManager.persist(pkg);
    }

    @Test
    public void whenFindByKeycloakUserIdAndStatus_thenReturnReservations() {
        TourPackageEntity pkg = createMockPackage();

        ReservationEntity res = new ReservationEntity();
        res.setKeycloakUserId("user123");
        res.setTourPackage(pkg);
        res.setPassengersCount(2);
        res.setTotalAmount(2000.0);
        res.setFinalAmount(1800.0);
        res.setDiscountPercentage(10.0);
        res.setStatus(ReservationStatusEnum.PAGADA);
        res.setPaidAt(LocalDateTime.now().minusDays(2));
        entityManager.persistAndFlush(res);

        List<ReservationEntity> found = reservationRepository.findByKeycloakUserIdAndStatus("user123", ReservationStatusEnum.PAGADA);

        assertThat(found).hasSize(1);
    }

    @Test
    public void whenFindByKeycloakUserIdAndStatusAndPaidAtAfter_thenReturnReservations() {
        TourPackageEntity pkg = createMockPackage();

        ReservationEntity res = new ReservationEntity();
        res.setKeycloakUserId("user123");
        res.setTourPackage(pkg);
        res.setPassengersCount(2);
        res.setTotalAmount(2000.0);
        res.setFinalAmount(1800.0);
        res.setDiscountPercentage(10.0);
        res.setStatus(ReservationStatusEnum.PAGADA);
        res.setPaidAt(LocalDateTime.now().minusDays(2));
        entityManager.persistAndFlush(res);

        List<ReservationEntity> found = reservationRepository.findByKeycloakUserIdAndStatusAndPaidAtAfter(
            "user123", ReservationStatusEnum.PAGADA, LocalDateTime.now().minusDays(5)
        );

        assertThat(found).hasSize(1);
    }
}
