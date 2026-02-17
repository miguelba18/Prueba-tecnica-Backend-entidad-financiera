package com.financiera.backend.service;

import com.financiera.backend.dto.ClienteDTO;
import com.financiera.backend.entity.Cliente;
import com.financiera.backend.exception.clientes.ClienteMenorDeEdadException;
import com.financiera.backend.exception.clientes.DatoDuplicadoException;
import com.financiera.backend.exception.clientes.RecursoNoEncontradoException;
import com.financiera.backend.repository.ClienteRepository;
import com.financiera.backend.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteDTO clienteDTO;
    private Cliente cliente;

    @BeforeEach
    void setUp() {

        clienteDTO = new ClienteDTO();
        clienteDTO.setTipoIdentificacion("CC");
        clienteDTO.setNumeroIdentificacion("1234567890");
        clienteDTO.setNombres("Juan Carlos");
        clienteDTO.setApellido("Pérez García");
        clienteDTO.setCorreoElectronico("juan.perez@example.com");
        clienteDTO.setFechaNacimiento(LocalDate.of(1990, 5, 15));

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setTipoIdentificacion("CC");
        cliente.setNumeroIdentificacion("1234567890");
        cliente.setNombres("Juan Carlos");
        cliente.setApellido("Pérez García");
        cliente.setCorreoElectronico("juan.perez@example.com");
        cliente.setFechaNacimiento(LocalDate.of(1990, 5, 15));
    }

    @Test
    void cuandoCrearCliente_entoncesRetornaClienteCreado() {

        when(clienteRepository.existsByNumeroIdentificacion(anyString())).thenReturn(false);
        when(clienteRepository.existsByCorreoElectronico(anyString())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        ClienteDTO resultado = clienteService.crearCliente(clienteDTO);

        assertNotNull(resultado);
        assertEquals("Juan Carlos", resultado.getNombres());
        assertEquals("1234567890", resultado.getNumeroIdentificacion());

        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void cuandoCrearClienteMenorDeEdad_entoncesLanzaExcepcion() {

        clienteDTO.setFechaNacimiento(LocalDate.now().minusYears(15));

        assertThrows(ClienteMenorDeEdadException.class, () -> {
            clienteService.crearCliente(clienteDTO);
        });

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void cuandoCrearClienteConIdentificacionDuplicada_entoncesLanzaExcepcion() {
        when(clienteRepository.existsByNumeroIdentificacion("1234567890")).thenReturn(true);

        assertThrows(DatoDuplicadoException.class, () -> {
            clienteService.crearCliente(clienteDTO);
        });

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void cuandoObtenerClientePorId_entoncesRetornaCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        ClienteDTO resultado = clienteService.obtenerClientePorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan Carlos", resultado.getNombres());
    }

    @Test
    void cuandoObtenerClienteInexistente_entoncesLanzaExcepcion() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> {
            clienteService.obtenerClientePorId(999L);
        });
    }

    @Test
    void cuandoActualizarCliente_entoncesRetornaClienteActualizado() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        clienteDTO.setNumeroIdentificacion(cliente.getNumeroIdentificacion());
        clienteDTO.setCorreoElectronico(cliente.getCorreoElectronico());
        clienteDTO.setNombres("Juan Updated");

        ClienteDTO resultado = clienteService.actualizarCliente(1L, clienteDTO);

        assertNotNull(resultado);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void cuandoEliminarCliente_entoncesEliminaCorrectamente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(productoRepository.countByClienteId(1L)).thenReturn(0L);

        clienteService.eliminarCliente(1L);

        verify(clienteRepository, times(1)).delete(cliente);
    }
}