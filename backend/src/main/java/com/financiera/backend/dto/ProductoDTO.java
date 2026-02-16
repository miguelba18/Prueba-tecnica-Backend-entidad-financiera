package com.financiera.backend.dto;

import com.financiera.backend.entity.Producto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

    private Long id;

    @NotNull(message = "El tipo de cuenta es obligatorio")
    private Producto.TipoCuenta tipoCuenta;

    private String numeroCuenta;

    private Producto.EstadoCuenta estado;

    private BigDecimal saldo;

    private Boolean exentaGMF;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaModificacion;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;


    private String nombreCliente;
}