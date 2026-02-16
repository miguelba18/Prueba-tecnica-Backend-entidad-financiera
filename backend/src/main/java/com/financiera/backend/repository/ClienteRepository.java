package com.financiera.backend.repository;

import com.financiera.backend.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByNumeroIdentificacion(String numeroIdentificacion);

    boolean existsByNumeroIdentificacion(String numeroIdentificacion);

    boolean existsByCorreoElectronico(String correoElectronico);
}