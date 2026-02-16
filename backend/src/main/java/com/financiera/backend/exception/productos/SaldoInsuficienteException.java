package com.financiera.backend.exception.productos;

public class SaldoInsuficienteException extends RuntimeException {
    public SaldoInsuficienteException(String message) {

        super(message);
    }
}
