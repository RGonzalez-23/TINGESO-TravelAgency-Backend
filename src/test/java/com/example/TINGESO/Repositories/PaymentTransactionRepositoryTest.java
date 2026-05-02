package com.example.TINGESO.Repositories;

import com.example.TINGESO.Entities.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

@DataJpaTest
class PaymentTransactionRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Test
    public void whenSavePayment_thenReturnPayment() {
        TourPackageEntity pkg = new TourPackageEntity();
        pkg.setName("Test"); pkg.setDestination("D"); pkg.setDescription("D");
        pkg.setStartDate(LocalDateTime.now().plusDays(1)); pkg.setEndDate(LocalDateTime.now().plusDays(2));
        pkg.setDurationDays(1); pkg.setPrice(10.0); pkg.setTotalSlots(10); pkg.setAvailableSlots(10);
        pkg.setTripType(TripTypeEnum.NACIONAL); pkg.setSeason(SeasonEnum.ALTA);
        pkg.setCategory(CategoryEnum.AVENTURA); pkg.setStatus(PackageStatusEnum.DISPONIBLE); pkg.setIsVisible(true);
        entityManager.persist(pkg);

        ReservationEntity res = new ReservationEntity();
        res.setKeycloakUserId("user"); res.setTourPackage(pkg); res.setPassengersCount(1);
        res.setTotalAmount(10.0); res.setFinalAmount(10.0); res.setDiscountPercentage(0.0);
        res.setStatus(ReservationStatusEnum.PAGADA);
        entityManager.persist(res);

        PaymentTransactionEntity pt = new PaymentTransactionEntity();
        pt.setTransactionHash("HASH123");
        pt.setAmountPaid(10.0);
        pt.setTransactionDate(LocalDateTime.now());
        pt.setPaymentMethod("CREDIT");
        pt.setReservation(res);
        entityManager.persistAndFlush(pt);

        PaymentTransactionEntity found = paymentTransactionRepository.findById(pt.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getTransactionHash()).isEqualTo("HASH123");
    }
}
