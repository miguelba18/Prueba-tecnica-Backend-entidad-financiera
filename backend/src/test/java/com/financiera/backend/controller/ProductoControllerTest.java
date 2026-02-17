package com.financiera.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financiera.backend.dto.ProductoDTO;
import com.financiera.backend.entity.Producto;
import com.financiera.backend.exception.productos.CuentaNoPuedeCancelarseException;
import com.financiera.backend.exception.clientes.RecursoNoEncontradoException;
import com.financiera.backend.service.ProductoService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    private ObjectMapper objectMapper;
    private ProductoDTO productoDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        productoDTO = new ProductoDTO();
        productoDTO.setId(1L);
        productoDTO.setTipoCuenta(Producto.TipoCuenta.CUENTA_AHORROS);
        productoDTO.setNumeroCuenta("5312345678");
        productoDTO.setEstado(Producto.EstadoCuenta.ACTIVA);
        productoDTO.setSaldo(BigDecimal.ZERO);
        productoDTO.setExentaGMF(false);
        productoDTO.setClienteId(1L);
        productoDTO.setNombreCliente("Juan Pérez");
    }

    @Test
    void cuandoCrearProducto_entoncesRetorna201() throws Exception {
        when(productoService.crearProducto(any(ProductoDTO.class))).thenReturn(productoDTO);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCuenta").value("5312345678"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.tipoCuenta").value("CUENTA_AHORROS"));
    }

    @Test
    void cuandoCrearProductoSinClienteId_entoncesRetorna400() throws Exception {
        productoDTO.setClienteId(null);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cuandoObtenerTodosLosProductos_entoncesRetorna200() throws Exception {
        when(productoService.obtenerTodosLosProductos()).thenReturn(List.of(productoDTO));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].numeroCuenta").value("5312345678"));
    }

    @Test
    void cuandoObtenerProductoPorId_entoncesRetorna200() throws Exception {
        when(productoService.obtenerProductoPorId(1L)).thenReturn(productoDTO);

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.numeroCuenta").value("5312345678"));
    }

    @Test
    void cuandoObtenerProductoInexistente_entoncesRetorna404() throws Exception {
        when(productoService.obtenerProductoPorId(999L))
                .thenThrow(new RecursoNoEncontradoException(
                        "No se encontró el producto con ID: 999"
                ));

        mockMvc.perform(get("/api/productos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No se encontró el producto con ID: 999"));
    }

    @Test
    void cuandoObtenerProductosPorCliente_entoncesRetorna200() throws Exception {
        when(productoService.obtenerProductosPorCliente(1L)).thenReturn(List.of(productoDTO));

        mockMvc.perform(get("/api/productos/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void cuandoCambiarEstadoProducto_entoncesRetorna200() throws Exception {
        productoDTO.setEstado(Producto.EstadoCuenta.INACTIVA);
        when(productoService.actualizarEstado(eq(1L), eq(Producto.EstadoCuenta.INACTIVA)))
                .thenReturn(productoDTO);

        mockMvc.perform(patch("/api/productos/1/estado")
                        .param("estado", "INACTIVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVA"));
    }

    @Test
    void cuandoCancelarCuentaConSaldo_entoncesRetorna409() throws Exception {
        when(productoService.actualizarEstado(eq(1L), eq(Producto.EstadoCuenta.CANCELADA)))
                .thenThrow(new CuentaNoPuedeCancelarseException(
                        "No se puede cancelar la cuenta. El saldo debe ser $0"
                ));

        mockMvc.perform(patch("/api/productos/1/estado")
                        .param("estado", "CANCELADA"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje")
                        .value("No se puede cancelar la cuenta. El saldo debe ser $0"));
    }

    @Test
    void cuandoEliminarProducto_entoncesRetorna204() throws Exception {
        doNothing().when(productoService).eliminarProducto(1L);

        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent());

        verify(productoService, times(1)).eliminarProducto(1L);
    }
}