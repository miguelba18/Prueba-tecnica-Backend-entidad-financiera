package com.financiera.backend.service;

import com.financiera.backend.dto.ProductoDTO;
import com.financiera.backend.entity.Cliente;
import com.financiera.backend.entity.Producto;
import com.financiera.backend.exception.productos.CuentaNoPuedeCancelarseException;
import com.financiera.backend.exception.clientes.RecursoNoEncontradoException;
import com.financiera.backend.repository.ClienteRepository;
import com.financiera.backend.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ProductoService productoService;

    private Cliente cliente;
    private Producto productoAhorros;
    private Producto productoCorriente;
    private ProductoDTO productoDTO;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombres("Juan Carlos");
        cliente.setApellido("Pérez");

        productoAhorros = new Producto();
        productoAhorros.setId(1L);
        productoAhorros.setTipoCuenta(Producto.TipoCuenta.CUENTA_AHORROS);
        productoAhorros.setNumeroCuenta("5312345678");
        productoAhorros.setEstado(Producto.EstadoCuenta.ACTIVA);
        productoAhorros.setSaldo(BigDecimal.ZERO);
        productoAhorros.setExentaGMF(false);
        productoAhorros.setCliente(cliente);

        productoCorriente = new Producto();
        productoCorriente.setId(2L);
        productoCorriente.setTipoCuenta(Producto.TipoCuenta.CUENTA_CORRIENTE);
        productoCorriente.setNumeroCuenta("3312345678");
        productoCorriente.setEstado(Producto.EstadoCuenta.ACTIVA);
        productoCorriente.setSaldo(BigDecimal.ZERO);
        productoCorriente.setExentaGMF(false);
        productoCorriente.setCliente(cliente);

        productoDTO = new ProductoDTO();
        productoDTO.setTipoCuenta(Producto.TipoCuenta.CUENTA_AHORROS);
        productoDTO.setClienteId(1L);
        productoDTO.setExentaGMF(false);
    }

    @Test
    void cuandoCrearCuentaAhorros_entoncesNumeroCuentaIniciaEn53() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(productoRepository.existsByNumeroCuenta(anyString())).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenReturn(productoAhorros);

        ProductoDTO resultado = productoService.crearProducto(productoDTO);

        assertNotNull(resultado);
        assertTrue(resultado.getNumeroCuenta().startsWith("53"));
        assertEquals(Producto.EstadoCuenta.ACTIVA, resultado.getEstado());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void cuandoCrearCuentaCorriente_entoncesNumeroCuentaIniciaEn33() {
        productoDTO.setTipoCuenta(Producto.TipoCuenta.CUENTA_CORRIENTE);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(productoRepository.existsByNumeroCuenta(anyString())).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenReturn(productoCorriente);

        ProductoDTO resultado = productoService.crearProducto(productoDTO);

        assertNotNull(resultado);
        assertTrue(resultado.getNumeroCuenta().startsWith("33"));
        assertEquals(Producto.EstadoCuenta.ACTIVA, resultado.getEstado());
    }

    @Test
    void cuandoCrearProductoConClienteInexistente_entoncesLanzaExcepcion() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());
        productoDTO.setClienteId(999L);

        assertThrows(RecursoNoEncontradoException.class, () -> {
            productoService.crearProducto(productoDTO);
        });

        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void cuandoObtenerProductoPorId_entoncesRetornaProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoAhorros));

        ProductoDTO resultado = productoService.obtenerProductoPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("5312345678", resultado.getNumeroCuenta());
    }

    @Test
    void cuandoObtenerProductoInexistente_entoncesLanzaExcepcion() {
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> {
            productoService.obtenerProductoPorId(999L);
        });
    }

    @Test
    void cuandoObtenerProductosPorCliente_entoncesRetornaLista() {
        when(clienteRepository.existsById(1L)).thenReturn(true);
        when(productoRepository.findByClienteId(1L))
                .thenReturn(List.of(productoAhorros, productoCorriente));

        List<ProductoDTO> resultado = productoService.obtenerProductosPorCliente(1L);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    void cuandoCambiarEstadoAInactiva_entoncesActualizaEstado() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoAhorros));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoAhorros);

        ProductoDTO resultado = productoService.actualizarEstado(1L, Producto.EstadoCuenta.INACTIVA);

        assertNotNull(resultado);
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void cuandoCancelarCuentaConSaldo_entoncesLanzaExcepcion() {
        productoAhorros.setSaldo(new BigDecimal("500000"));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoAhorros));

        assertThrows(CuentaNoPuedeCancelarseException.class, () -> {
            productoService.actualizarEstado(1L, Producto.EstadoCuenta.CANCELADA);
        });

        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void cuandoCancelarCuentaConSaldoCero_entoncesSeCancela() {
        productoAhorros.setSaldo(BigDecimal.ZERO);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoAhorros));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoAhorros);

        ProductoDTO resultado = productoService.actualizarEstado(1L, Producto.EstadoCuenta.CANCELADA);

        assertNotNull(resultado);
        verify(productoRepository, times(1)).save(any(Producto.class));
    }
}