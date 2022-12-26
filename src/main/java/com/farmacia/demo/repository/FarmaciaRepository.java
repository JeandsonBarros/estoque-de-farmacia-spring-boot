package com.farmacia.demo.repository;

import com.farmacia.demo.models.Farmacia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FarmaciaRepository extends JpaRepository<Farmacia, Long> {

    Optional<Farmacia> findByEmail(String email);
    List<Farmacia> findByNomeContaining(String nome);
}
