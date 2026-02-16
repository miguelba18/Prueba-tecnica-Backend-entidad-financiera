package com.financiera.backend.exception.clientes;

public class ClienteMenorDeEdadException extends RuntimeException {
    public ClienteMenorDeEdadException(String mensaje) {
        super(mensaje);
    }
}