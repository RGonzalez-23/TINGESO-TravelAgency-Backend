package com.example.TINGESO.Repositories;

import com.example.TINGESO.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    // Nuevo método para validar unicidad del RUT
    boolean existsByUserRut(String userRut);
}