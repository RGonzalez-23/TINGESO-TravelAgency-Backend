package com.example.TINGESO.Repositories;

import com.example.TINGESO.Entities.GlobalDiscountConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalDiscountConfigRepository extends JpaRepository<GlobalDiscountConfigEntity, Long> {
}
