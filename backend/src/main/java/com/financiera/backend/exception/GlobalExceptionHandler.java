package com.financiera.backend.exception;

import com.financiera.backend.exception.clientes.ClienteConProductosException;
import com.financiera.backend.exception.clientes.ClienteMenorDeEdadException;
import com.financiera.backend.exception.clientes.DatoDuplicadoException;
import com.financiera.backend.exception.clientes.RecursoNoEncontradoException;

import com.financiera.backend.exception.productos.CuentaNoPuedeCancelarseException;
import com.financiera.backend.exception.productos.OperacionNoPermitidaException;
import com.financiera.backend.exception.productos.SaldoInsuficienteException;

import com.financiera.backend.exception.transacciones.CuentaInactivaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClienteMenorDeEdadException.class)
    public ResponseEntity<Map<String, Object>> manejarClienteMenorDeEdad(ClienteMenorDeEdadException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("mensaje", ex.getMessage());
        respuesta.put("status", HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ClienteConProductosException.class)
    public ResponseEntity<Map<String, Object>> manejarClienteConProductos(ClienteConProductosException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("mensaje", ex.getMessage());
        respuesta.put("status", HttpStatus.CONFLICT.value());

        return new ResponseEntity<>(respuesta, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("mensaje", ex.getMessage());
        respuesta.put("status", HttpStatus.NOT_FOUND.value());

        return new ResponseEntity<>(respuesta, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DatoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> manejarDatoDuplicado(DatoDuplicadoException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("mensaje", ex.getMessage());
        respuesta.put("status", HttpStatus.CONFLICT.value());

        return new ResponseEntity<>(respuesta, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("mensaje", "Error de validación");
        respuesta.put("errores", errores);
        respuesta.put("status", HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> manejarSaldoInsuficiente(SaldoInsuficienteException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("mensaje", ex.getMessage());
        respuesta.put("status", HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CuentaNoPuedeCancelarseException.class)
    public ResponseEntity<Map<String, Object>> manejarCuentaNoPuedeCancelarse(CuentaNoPuedeCancelarseException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("mensaje", ex.getMessage());
        respuesta.put("status", HttpStatus.CONFLICT.value());

        return new ResponseEntity<>(respuesta, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<Map<String, Object>> manejarOperacionNoPermitida(OperacionNoPermitidaException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("mensaje", ex.getMessage());
        respuesta.put("status", HttpStatus.FORBIDDEN.value());

        return new ResponseEntity<>(respuesta, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(CuentaInactivaException.class)
    public ResponseEntity<Map<String, Object>> manejarCuentaInactiva(CuentaInactivaException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("mensaje", ex.getMessage());
        respuesta.put("status", HttpStatus.FORBIDDEN.value());

        return new ResponseEntity<>(respuesta, HttpStatus.FORBIDDEN);
    }


}