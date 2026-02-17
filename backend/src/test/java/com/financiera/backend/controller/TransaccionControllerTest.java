package com.financiera.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financiera.backend.dto.ConsignacionDTO;
import com.financiera.backend.dto.RetiroDTO;
import com.financiera.backend.dto.TransaccionDTO;
import com.financiera.backend.dto.TransferenciaDTO;
import com.financiera.backend.entity.Transaccion;
import com.financiera.backend.exception.productos.SaldoInsuficienteException;
import com.financiera.backend.exception.transacciones.CuentaInactivaException;
import com.financiera.backend.service.TransaccionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransaccionController.class)
class TransaccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransaccionService transaccionService;

    private ObjectMapper objectMapper;
    private TransaccionDTO transaccionDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        transaccionDTO = new TransaccionDTO();
        transaccionDTO.setId(1L);
        transaccionDTO.setTipoTransaccion(Transaccion.TipoTransaccion.CONSIGNACION);
        transaccionDTO.setTipoMovimiento(Transaccion.TipoMovimiento.CREDITO);
        transaccionDTO.setMonto(new BigDecimal("500000"));
        transaccionDTO.setSaldoDespues(new BigDecimal("1500000"));
        transaccionDTO.setCuentaOrigenId(1L);
        transaccionDTO.setNumeroCuentaOrigen("5312345678");
    }

    @Test
    void cuandoRealizarConsignacion_entoncesRetorna201() throws Exception {
        ConsignacionDTO consignacionDTO = new ConsignacionDTO();
        consignacionDTO.setCuentaId(1L);
        consignacionDTO.setMonto(new BigDecimal("500000"));
        consignacionDTO.setDescripcion("Consignación test");

        when(transaccionService.realizarConsignacion(any(ConsignacionDTO.class)))
                .thenReturn(transaccionDTO);

        mockMvc.perform(post("/api/transacciones/consignacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consignacionDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMovimiento").value("CREDITO"))
                .andExpect(jsonPath("$.monto").value(500000));
    }

    @Test
    void cuandoConsignacionEnCuentaInactiva_entoncesRetorna403() throws Exception {
        ConsignacionDTO consignacionDTO = new ConsignacionDTO();
        consignacionDTO.setCuentaId(1L);
        consignacionDTO.setMonto(new BigDecimal("500000"));

        when(transaccionService.realizarConsignacion(any(ConsignacionDTO.class)))
                .thenThrow(new CuentaInactivaException("La cuenta no está activa"));

        mockMvc.perform(post("/api/transacciones/consignacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consignacionDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("La cuenta no está activa"));
    }

    @Test
    void cuandoRealizarRetiro_entoncesRetorna201() throws Exception {
        RetiroDTO retiroDTO = new RetiroDTO();
        retiroDTO.setCuentaId(1L);
        retiroDTO.setMonto(new BigDecimal("200000"));

        transaccionDTO.setTipoTransaccion(Transaccion.TipoTransaccion.RETIRO);
        transaccionDTO.setTipoMovimiento(Transaccion.TipoMovimiento.DEBITO);

        when(transaccionService.realizarRetiro(any(RetiroDTO.class)))
                .thenReturn(transaccionDTO);

        mockMvc.perform(post("/api/transacciones/retiro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retiroDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMovimiento").value("DEBITO"));
    }

    @Test
    void cuandoRetiroConSaldoInsuficiente_entoncesRetorna400() throws Exception {
        RetiroDTO retiroDTO = new RetiroDTO();
        retiroDTO.setCuentaId(1L);
        retiroDTO.setMonto(new BigDecimal("9999999"));

        when(transaccionService.realizarRetiro(any(RetiroDTO.class)))
                .thenThrow(new SaldoInsuficienteException("Saldo insuficiente"));

        mockMvc.perform(post("/api/transacciones/retiro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retiroDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Saldo insuficiente"));
    }

    @Test
    void cuandoRealizarTransferencia_entoncesRetorna201ConDosTransacciones() throws Exception {
        TransferenciaDTO transferenciaDTO = new TransferenciaDTO();
        transferenciaDTO.setCuentaOrigenId(1L);
        transferenciaDTO.setCuentaDestinoId(2L);
        transferenciaDTO.setMonto(new BigDecimal("300000"));

        TransaccionDTO debito = new TransaccionDTO();
        debito.setTipoMovimiento(Transaccion.TipoMovimiento.DEBITO);
        debito.setMonto(new BigDecimal("300000"));
        debito.setCuentaOrigenId(1L);

        TransaccionDTO credito = new TransaccionDTO();
        credito.setTipoMovimiento(Transaccion.TipoMovimiento.CREDITO);
        credito.setMonto(new BigDecimal("300000"));
        credito.setCuentaOrigenId(2L);

        when(transaccionService.realizarTransferencia(any(TransferenciaDTO.class)))
                .thenReturn(List.of(debito, credito));

        mockMvc.perform(post("/api/transacciones/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferenciaDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipoMovimiento").value("DEBITO"))
                .andExpect(jsonPath("$[1].tipoMovimiento").value("CREDITO"));
    }

    @Test
    void cuandoObtenerEstadoCuenta_entoncesRetorna200() throws Exception {
        when(transaccionService.obtenerEstadoCuenta(1L))
                .thenReturn(List.of(transaccionDTO));

        mockMvc.perform(get("/api/transacciones/estado-cuenta/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipoTransaccion").value("CONSIGNACION"));
    }

    @Test
    void cuandoObtenerTransaccionPorId_entoncesRetorna200() throws Exception {
        when(transaccionService.obtenerTransaccionPorId(1L)).thenReturn(transaccionDTO);

        mockMvc.perform(get("/api/transacciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}