package com.financiera.backend.exception.productos;

public class OperacionNoPermitidaException extends RuntimeException {
    public OperacionNoPermitidaException(String message) {

        super(message);
    }
}
