package com.financiera.backend.service;

import com.financiera.backend.dto.*;
import com.financiera.backend.entity.Producto;
import com.financiera.backend.entity.Transaccion;
import com.financiera.backend.exception.transacciones.CuentaInactivaException;
import com.financiera.backend.exception.clientes.RecursoNoEncontradoException;
import com.financiera.backend.exception.productos.OperacionNoPermitidaException;
import com.financiera.backend.exception.productos.SaldoInsuficienteException;

import com.financiera.backend.repository.ProductoRepository;
import com.financiera.backend.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final ProductoRepository productoRepository;

    // Realizar consignación (depósito)
    @Transactional
    public TransaccionDTO realizarConsignacion(ConsignacionDTO consignacionDTO) {

        Producto cuenta = productoRepository.findById(consignacionDTO.getCuentaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la cuenta con ID: " + consignacionDTO.getCuentaId()
                ));
        validarCuentaActiva(cuenta);

        Transaccion transaccion = new Transaccion();
        transaccion.setTipoTransaccion(Transaccion.TipoTransaccion.CONSIGNACION);
        transaccion.setTipoMovimiento(Transaccion.TipoMovimiento.CREDITO);
        transaccion.setMonto(consignacionDTO.getMonto());
        transaccion.setDescripcion(consignacionDTO.getDescripcion() != null ?
                consignacionDTO.getDescripcion() : "Consignación");
        transaccion.setCuentaOrigen(cuenta);

        BigDecimal nuevoSaldo = cuenta.getSaldo().add(consignacionDTO.getMonto());
        cuenta.setSaldo(nuevoSaldo);
        transaccion.setSaldoDespues(nuevoSaldo);

        productoRepository.save(cuenta);

        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);

        return convertirEntityADTO(transaccionGuardada);
    }

    // Realizar retiro
    @Transactional
    public TransaccionDTO realizarRetiro(RetiroDTO retiroDTO) {
        Producto cuenta = productoRepository.findById(retiroDTO.getCuentaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la cuenta con ID: " + retiroDTO.getCuentaId()
                ));


        validarCuentaActiva(cuenta);

        validarSaldoSuficiente(cuenta, retiroDTO.getMonto());

        Transaccion transaccion = new Transaccion();
        transaccion.setTipoTransaccion(Transaccion.TipoTransaccion.RETIRO);
        transaccion.setTipoMovimiento(Transaccion.TipoMovimiento.DEBITO);
        transaccion.setMonto(retiroDTO.getMonto());
        transaccion.setDescripcion(retiroDTO.getDescripcion() != null ?
                retiroDTO.getDescripcion() : "Retiro");
        transaccion.setCuentaOrigen(cuenta);

        BigDecimal nuevoSaldo = cuenta.getSaldo().subtract(retiroDTO.getMonto());
        cuenta.setSaldo(nuevoSaldo);
        transaccion.setSaldoDespues(nuevoSaldo);

        productoRepository.save(cuenta);

        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);

        return convertirEntityADTO(transaccionGuardada);
    }

    // Realizar transferencia
    @Transactional
    public List<TransaccionDTO> realizarTransferencia(TransferenciaDTO transferenciaDTO) {
        if (transferenciaDTO.getCuentaOrigenId().equals(transferenciaDTO.getCuentaDestinoId())) {
            throw new OperacionNoPermitidaException(
                    "No se puede transferir a la misma cuenta"
            );
        }

        Producto cuentaOrigen = productoRepository.findById(transferenciaDTO.getCuentaOrigenId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la cuenta origen con ID: " + transferenciaDTO.getCuentaOrigenId()
                ));

        Producto cuentaDestino = productoRepository.findById(transferenciaDTO.getCuentaDestinoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la cuenta destino con ID: " + transferenciaDTO.getCuentaDestinoId()
                ));

        validarCuentaActiva(cuentaOrigen);
        validarCuentaActiva(cuentaDestino);

        validarSaldoSuficiente(cuentaOrigen, transferenciaDTO.getMonto());

        Transaccion transaccionDebito = new Transaccion();
        transaccionDebito.setTipoTransaccion(Transaccion.TipoTransaccion.TRANSFERENCIA);
        transaccionDebito.setTipoMovimiento(Transaccion.TipoMovimiento.DEBITO);
        transaccionDebito.setMonto(transferenciaDTO.getMonto());
        transaccionDebito.setDescripcion(transferenciaDTO.getDescripcion() != null ?
                transferenciaDTO.getDescripcion() :
                "Transferencia a cuenta " + cuentaDestino.getNumeroCuenta());
        transaccionDebito.setCuentaOrigen(cuentaOrigen);
        transaccionDebito.setCuentaDestino(cuentaDestino);

        BigDecimal nuevoSaldoOrigen = cuentaOrigen.getSaldo().subtract(transferenciaDTO.getMonto());
        cuentaOrigen.setSaldo(nuevoSaldoOrigen);
        transaccionDebito.setSaldoDespues(nuevoSaldoOrigen);

        Transaccion transaccionCredito = new Transaccion();
        transaccionCredito.setTipoTransaccion(Transaccion.TipoTransaccion.TRANSFERENCIA);
        transaccionCredito.setTipoMovimiento(Transaccion.TipoMovimiento.CREDITO);
        transaccionCredito.setMonto(transferenciaDTO.getMonto());
        transaccionCredito.setDescripcion(transferenciaDTO.getDescripcion() != null ?
                transferenciaDTO.getDescripcion() :
                "Transferencia desde cuenta " + cuentaOrigen.getNumeroCuenta());
        transaccionCredito.setCuentaOrigen(cuentaDestino);
        transaccionCredito.setCuentaDestino(cuentaOrigen);


        BigDecimal nuevoSaldoDestino = cuentaDestino.getSaldo().add(transferenciaDTO.getMonto());
        cuentaDestino.setSaldo(nuevoSaldoDestino);
        transaccionCredito.setSaldoDespues(nuevoSaldoDestino);


        productoRepository.save(cuentaOrigen);
        productoRepository.save(cuentaDestino);


        Transaccion debitoGuardado = transaccionRepository.save(transaccionDebito);
        Transaccion creditoGuardado = transaccionRepository.save(transaccionCredito);


        return List.of(
                convertirEntityADTO(debitoGuardado),
                convertirEntityADTO(creditoGuardado)
        );
    }

    // Obtener estado de cuenta (historial de transacciones)
    @Transactional(readOnly = true)
    public List<TransaccionDTO> obtenerEstadoCuenta(Long cuentaId) {
        // Validar que la cuenta existe
        if (!productoRepository.existsById(cuentaId)) {
            throw new RecursoNoEncontradoException(
                    "No se encontró la cuenta con ID: " + cuentaId
            );
        }

        return transaccionRepository.findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaTransaccionDesc(
                        cuentaId, cuentaId)
                .stream()
                .map(this::convertirEntityADTO)
                .collect(Collectors.toList());
    }

    // Obtener transacción por ID
    @Transactional(readOnly = true)
    public TransaccionDTO obtenerTransaccionPorId(Long id) {
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la transacción con ID: " + id
                ));
        return convertirEntityADTO(transaccion);
    }

    // Metodos auxiliares

    private void validarCuentaActiva(Producto cuenta) {
        if (cuenta.getEstado() != Producto.EstadoCuenta.ACTIVA) {
            throw new CuentaInactivaException(
                    "La cuenta " + cuenta.getNumeroCuenta() + " no está activa. Estado: " + cuenta.getEstado()
            );
        }
    }

    private void validarSaldoSuficiente(Producto cuenta, BigDecimal monto) {
        BigDecimal nuevoSaldo = cuenta.getSaldo().subtract(monto);

        if (cuenta.getTipoCuenta() == Producto.TipoCuenta.CUENTA_AHORROS) {
            if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
                throw new SaldoInsuficienteException(
                        "Saldo insuficiente. Saldo actual: $" + cuenta.getSaldo() +
                                ", Monto a retirar: $" + monto +
                                ". Las cuentas de ahorros no pueden tener saldo negativo."
                );
            }
        }

    }

    private TransaccionDTO convertirEntityADTO(Transaccion transaccion) {
        TransaccionDTO dto = new TransaccionDTO();
        dto.setId(transaccion.getId());
        dto.setTipoTransaccion(transaccion.getTipoTransaccion());
        dto.setTipoMovimiento(transaccion.getTipoMovimiento());
        dto.setMonto(transaccion.getMonto());
        dto.setDescripcion(transaccion.getDescripcion());
        dto.setFechaTransaccion(transaccion.getFechaTransaccion());
        dto.setSaldoDespues(transaccion.getSaldoDespues());

        dto.setCuentaOrigenId(transaccion.getCuentaOrigen().getId());
        dto.setNumeroCuentaOrigen(transaccion.getCuentaOrigen().getNumeroCuenta());
        
        if (transaccion.getCuentaDestino() != null) {
            dto.setCuentaDestinoId(transaccion.getCuentaDestino().getId());
            dto.setNumeroCuentaDestino(transaccion.getCuentaDestino().getNumeroCuenta());
        }

        return dto;
    }
}