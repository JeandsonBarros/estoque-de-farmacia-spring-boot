package com.farmacia.demo.repository;

import com.farmacia.demo.models.Produto;
import com.farmacia.demo.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByNomeContaining(String nome);
}
