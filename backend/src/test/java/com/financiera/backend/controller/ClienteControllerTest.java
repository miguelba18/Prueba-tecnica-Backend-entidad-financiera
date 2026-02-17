package com.financiera.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financiera.backend.dto.ClienteDTO;
import com.financiera.backend.exception.clientes.ClienteMenorDeEdadException;
import com.financiera.backend.exception.clientes.RecursoNoEncontradoException;
import com.financiera.backend.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    private ObjectMapper objectMapper;
    private ClienteDTO clienteDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        clienteDTO = new ClienteDTO();
        clienteDTO.setId(1L);
        clienteDTO.setTipoIdentificacion("CC");
        clienteDTO.setNumeroIdentificacion("1234567890");
        clienteDTO.setNombres("Juan Carlos");
        clienteDTO.setApellido("Pérez García");
        clienteDTO.setCorreoElectronico("juan.perez@example.com");
        clienteDTO.setFechaNacimiento(LocalDate.of(1990, 5, 15));
    }

    @Test
    void cuandoCrearCliente_entoncesRetorna201() throws Exception {

        when(clienteService.crearCliente(any(ClienteDTO.class))).thenReturn(clienteDTO);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombres").value("Juan Carlos"))
                .andExpect(jsonPath("$.correoElectronico").value("juan.perez@example.com"))
                .andExpect(jsonPath("$.numeroIdentificacion").value("1234567890"));
    }

    @Test
    void cuandoCrearClienteSinNombre_entoncesRetorna400() throws Exception {

        clienteDTO.setNombres("");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cuandoCrearClienteMenorDeEdad_entoncesRetorna400() throws Exception {
        when(clienteService.crearCliente(any(ClienteDTO.class)))
                .thenThrow(new ClienteMenorDeEdadException(
                        "El cliente debe ser mayor de edad"
                ));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El cliente debe ser mayor de edad"));
    }

    @Test
    void cuandoObtenerTodosLosClientes_entoncesRetorna200() throws Exception {
        when(clienteService.obtenerTodosLosClientes()).thenReturn(List.of(clienteDTO));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombres").value("Juan Carlos"));
    }

    @Test
    void cuandoObtenerClientePorId_entoncesRetorna200() throws Exception {
        when(clienteService.obtenerClientePorId(1L)).thenReturn(clienteDTO);

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombres").value("Juan Carlos"));
    }

    @Test
    void cuandoObtenerClienteInexistente_entoncesRetorna404() throws Exception {
        when(clienteService.obtenerClientePorId(999L))
                .thenThrow(new RecursoNoEncontradoException(
                        "No se encontró el cliente con ID: 999"
                ));

        mockMvc.perform(get("/api/clientes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No se encontró el cliente con ID: 999"));
    }

    @Test
    void cuandoActualizarCliente_entoncesRetorna200() throws Exception {
        when(clienteService.actualizarCliente(eq(1L), any(ClienteDTO.class))).thenReturn(clienteDTO);

        mockMvc.perform(put("/api/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void cuandoEliminarCliente_entoncesRetorna204() throws Exception {
        doNothing().when(clienteService).eliminarCliente(1L);

        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());

        verify(clienteService, times(1)).eliminarCliente(1L);
    }
}