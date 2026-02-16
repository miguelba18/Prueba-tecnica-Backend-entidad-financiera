package com.financiera.backend.exception;

public class ClienteMenorDeEdadException extends RuntimeException {
    public ClienteMenorDeEdadException(String mensaje) {
        super(mensaje);
    }
}