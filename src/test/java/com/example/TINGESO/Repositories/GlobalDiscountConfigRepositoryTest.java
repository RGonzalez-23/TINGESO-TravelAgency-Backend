package com.example.TINGESO.Repositories;

import com.example.TINGESO.Entities.GlobalDiscountConfigEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GlobalDiscountConfigRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private GlobalDiscountConfigRepository configRepository;

    @Test
    public void whenFindById_thenReturnConfig() {
        GlobalDiscountConfigEntity config = new GlobalDiscountConfigEntity();
        config.setGroupMinPassengers(5);
        entityManager.persistAndFlush(config);

        GlobalDiscountConfigEntity found = configRepository.findById(1L).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getGroupMinPassengers()).isEqualTo(5);
    }
}
