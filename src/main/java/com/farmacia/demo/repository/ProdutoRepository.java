package com.farmacia.demo.repository;

import com.farmacia.demo.models.Farmacia;
import com.farmacia.demo.models.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Optional<Produto> findByFarmaciaAndId(Farmacia farmacia, Long id);
    List<Produto> findByFarmaciaAndNomeContaining(Farmacia farmacia, String nome);
    List<Produto> findByFarmacia(Farmacia farmacia);

}
