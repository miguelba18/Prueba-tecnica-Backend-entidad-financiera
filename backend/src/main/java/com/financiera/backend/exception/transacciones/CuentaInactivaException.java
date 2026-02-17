package com.financiera.backend.exception.transacciones;

public class CuentaInactivaException extends RuntimeException {
    public CuentaInactivaException(String message) {

        super(message);
    }
}
