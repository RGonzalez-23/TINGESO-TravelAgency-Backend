package com.example.TINGESO.Repositories;

import com.example.TINGESO.Entities.PromotionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
class PromotionRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private PromotionRepository promotionRepository;

    @Test
    public void whenFindByIsActiveTrueAndStartDateBeforeAndEndDateAfter_thenReturnPromotions() {
        PromotionEntity promo = new PromotionEntity();
        promo.setName("Black Friday");
        promo.setDiscountPercentage(20.0);
        promo.setIsActive(true);
        promo.setStartDate(LocalDateTime.now().minusDays(1));
        promo.setEndDate(LocalDateTime.now().plusDays(5));
        entityManager.persistAndFlush(promo);

        List<PromotionEntity> found = promotionRepository.findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(LocalDateTime.now(), LocalDateTime.now());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Black Friday");
    }
}
