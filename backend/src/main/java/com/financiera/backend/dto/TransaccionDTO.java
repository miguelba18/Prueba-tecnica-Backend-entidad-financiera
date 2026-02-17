package com.financiera.backend.dto;

import com.financiera.backend.entity.Transaccion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionDTO {

    private Long id;
    private Transaccion.TipoTransaccion tipoTransaccion;
    private Transaccion.TipoMovimiento tipoMovimiento;
    private BigDecimal monto;
    private String descripcion;
    private LocalDateTime fechaTransaccion;

    // Información de las cuentas
    private Long cuentaOrigenId;
    private String numeroCuentaOrigen;
    private Long cuentaDestinoId;
    private String numeroCuentaDestino;

    private BigDecimal saldoDespues;
}