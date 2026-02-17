package com.financiera.backend.repository;

import com.financiera.backend.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {


    List<Transaccion> findByCuentaOrigenIdOrderByFechaTransaccionDesc(Long cuentaOrigenId);


    List<Transaccion> findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaTransaccionDesc(
            Long cuentaOrigenId, Long cuentaDestinoId);
}