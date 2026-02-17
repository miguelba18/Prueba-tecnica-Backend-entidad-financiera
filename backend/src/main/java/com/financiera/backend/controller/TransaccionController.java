package com.financiera.backend.controller;

import com.financiera.backend.dto.*;
import com.financiera.backend.service.TransaccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService transaccionService;


    @PostMapping("/consignacion")
    public ResponseEntity<TransaccionDTO> realizarConsignacion(
            @Valid @RequestBody ConsignacionDTO consignacionDTO) {
        TransaccionDTO transaccion = transaccionService.realizarConsignacion(consignacionDTO);
        return new ResponseEntity<>(transaccion, HttpStatus.CREATED);
    }


    @PostMapping("/retiro")
    public ResponseEntity<TransaccionDTO> realizarRetiro(
            @Valid @RequestBody RetiroDTO retiroDTO) {
        TransaccionDTO transaccion = transaccionService.realizarRetiro(retiroDTO);
        return new ResponseEntity<>(transaccion, HttpStatus.CREATED);
    }


    @PostMapping("/transferencia")
    public ResponseEntity<List<TransaccionDTO>> realizarTransferencia(
            @Valid @RequestBody TransferenciaDTO transferenciaDTO) {
        List<TransaccionDTO> transacciones = transaccionService.realizarTransferencia(transferenciaDTO);
        return new ResponseEntity<>(transacciones, HttpStatus.CREATED);
    }


    @GetMapping("/{id}")
    public ResponseEntity<TransaccionDTO> obtenerTransaccionPorId(@PathVariable Long id) {
        TransaccionDTO transaccion = transaccionService.obtenerTransaccionPorId(id);
        return ResponseEntity.ok(transaccion);
    }

    
    @GetMapping("/estado-cuenta/{cuentaId}")
    public ResponseEntity<List<TransaccionDTO>> obtenerEstadoCuenta(@PathVariable Long cuentaId) {
        List<TransaccionDTO> estadoCuenta = transaccionService.obtenerEstadoCuenta(cuentaId);
        return ResponseEntity.ok(estadoCuenta);
    }
}