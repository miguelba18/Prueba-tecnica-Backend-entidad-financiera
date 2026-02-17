package com.financiera.backend.service;

import com.financiera.backend.dto.ConsignacionDTO;
import com.financiera.backend.dto.RetiroDTO;
import com.financiera.backend.dto.TransaccionDTO;
import com.financiera.backend.dto.TransferenciaDTO;
import com.financiera.backend.entity.Cliente;
import com.financiera.backend.entity.Producto;
import com.financiera.backend.entity.Transaccion;
import com.financiera.backend.exception.transacciones.CuentaInactivaException;
import com.financiera.backend.exception.productos.OperacionNoPermitidaException;
import com.financiera.backend.exception.productos.SaldoInsuficienteException;
import com.financiera.backend.repository.ProductoRepository;
import com.financiera.backend.repository.TransaccionRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private TransaccionService transaccionService;

    private Producto cuentaAhorros;
    private Producto cuentaCorriente;
    private Transaccion transaccion;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombres("Juan");
        cliente.setApellido("Pérez");

        cuentaAhorros = new Producto();
        cuentaAhorros.setId(1L);
        cuentaAhorros.setTipoCuenta(Producto.TipoCuenta.CUENTA_AHORROS);
        cuentaAhorros.setNumeroCuenta("5312345678");
        cuentaAhorros.setEstado(Producto.EstadoCuenta.ACTIVA);
        cuentaAhorros.setSaldo(new BigDecimal("1000000"));
        cuentaAhorros.setCliente(cliente);

        cuentaCorriente = new Producto();
        cuentaCorriente.setId(2L);
        cuentaCorriente.setTipoCuenta(Producto.TipoCuenta.CUENTA_CORRIENTE);
        cuentaCorriente.setNumeroCuenta("3312345678");
        cuentaCorriente.setEstado(Producto.EstadoCuenta.ACTIVA);
        cuentaCorriente.setSaldo(new BigDecimal("500000"));
        cuentaCorriente.setCliente(cliente);

        transaccion = new Transaccion();
        transaccion.setId(1L);
        transaccion.setTipoTransaccion(Transaccion.TipoTransaccion.CONSIGNACION);
        transaccion.setTipoMovimiento(Transaccion.TipoMovimiento.CREDITO);
        transaccion.setMonto(new BigDecimal("500000"));
        transaccion.setSaldoDespues(new BigDecimal("1500000"));
        transaccion.setCuentaOrigen(cuentaAhorros);
    }

    @Test
    void cuandoRealizarConsignacion_entoncesAumentaSaldo() {
        ConsignacionDTO dto = new ConsignacionDTO();
        dto.setCuentaId(1L);
        dto.setMonto(new BigDecimal("500000"));
        dto.setDescripcion("Consignación test");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaAhorros));
        when(productoRepository.save(any(Producto.class))).thenReturn(cuentaAhorros);
        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccion);

        TransaccionDTO resultado = transaccionService.realizarConsignacion(dto);

        assertNotNull(resultado);
        assertEquals(Transaccion.TipoMovimiento.CREDITO, resultado.getTipoMovimiento());
        verify(productoRepository, times(1)).save(any(Producto.class));
        verify(transaccionRepository, times(1)).save(any(Transaccion.class));
    }

    @Test
    void cuandoConsignacionEnCuentaInactiva_entoncesLanzaExcepcion() {
        cuentaAhorros.setEstado(Producto.EstadoCuenta.INACTIVA);
        ConsignacionDTO dto = new ConsignacionDTO();
        dto.setCuentaId(1L);
        dto.setMonto(new BigDecimal("500000"));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaAhorros));

        assertThrows(CuentaInactivaException.class, () -> {
            transaccionService.realizarConsignacion(dto);
        });

        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }


    @Test
    void cuandoRealizarRetiro_entoncesDisminuyeSaldo() {
        RetiroDTO dto = new RetiroDTO();
        dto.setCuentaId(1L);
        dto.setMonto(new BigDecimal("200000"));
        dto.setDescripcion("Retiro test");

        Transaccion transaccionRetiro = new Transaccion();
        transaccionRetiro.setId(2L);
        transaccionRetiro.setTipoMovimiento(Transaccion.TipoMovimiento.DEBITO);
        transaccionRetiro.setMonto(new BigDecimal("200000"));
        transaccionRetiro.setSaldoDespues(new BigDecimal("800000"));
        transaccionRetiro.setCuentaOrigen(cuentaAhorros);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaAhorros));
        when(productoRepository.save(any(Producto.class))).thenReturn(cuentaAhorros);
        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccionRetiro);


        TransaccionDTO resultado = transaccionService.realizarRetiro(dto);

        assertNotNull(resultado);
        assertEquals(Transaccion.TipoMovimiento.DEBITO, resultado.getTipoMovimiento());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void cuandoRetiroConSaldoInsuficienteEnAhorros_entoncesLanzaExcepcion() {
        RetiroDTO dto = new RetiroDTO();
        dto.setCuentaId(1L);
        dto.setMonto(new BigDecimal("9999999"));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaAhorros));

        assertThrows(SaldoInsuficienteException.class, () -> {
            transaccionService.realizarRetiro(dto);
        });

        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }

    @Test
    void cuandoRetiroEnCuentaCorrienteSinSaldo_entoncesSeProcesa() {
        cuentaCorriente.setSaldo(new BigDecimal("100000"));
        RetiroDTO dto = new RetiroDTO();
        dto.setCuentaId(2L);
        dto.setMonto(new BigDecimal("200000"));

        Transaccion transaccionRetiro = new Transaccion();
        transaccionRetiro.setId(3L);
        transaccionRetiro.setTipoMovimiento(Transaccion.TipoMovimiento.DEBITO);
        transaccionRetiro.setMonto(new BigDecimal("200000"));
        transaccionRetiro.setSaldoDespues(new BigDecimal("-100000"));
        transaccionRetiro.setCuentaOrigen(cuentaCorriente);

        when(productoRepository.findById(2L)).thenReturn(Optional.of(cuentaCorriente));
        when(productoRepository.save(any(Producto.class))).thenReturn(cuentaCorriente);
        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccionRetiro);

        TransaccionDTO resultado = transaccionService.realizarRetiro(dto);

        assertNotNull(resultado);
        verify(transaccionRepository, times(1)).save(any(Transaccion.class));
    }


    @Test
    void cuandoRealizarTransferencia_entoncesGeneraDosTrasacciones() {
        // Given
        TransferenciaDTO dto = new TransferenciaDTO();
        dto.setCuentaOrigenId(1L);
        dto.setCuentaDestinoId(2L);
        dto.setMonto(new BigDecimal("300000"));
        dto.setDescripcion("Transferencia test");

        Transaccion debito = new Transaccion();
        debito.setId(1L);
        debito.setTipoMovimiento(Transaccion.TipoMovimiento.DEBITO);
        debito.setMonto(new BigDecimal("300000"));
        debito.setSaldoDespues(new BigDecimal("700000"));
        debito.setCuentaOrigen(cuentaAhorros);
        debito.setCuentaDestino(cuentaCorriente);

        Transaccion credito = new Transaccion();
        credito.setId(2L);
        credito.setTipoMovimiento(Transaccion.TipoMovimiento.CREDITO);
        credito.setMonto(new BigDecimal("300000"));
        credito.setSaldoDespues(new BigDecimal("800000"));
        credito.setCuentaOrigen(cuentaCorriente);
        credito.setCuentaDestino(cuentaAhorros);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaAhorros));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(cuentaCorriente));
        when(productoRepository.save(any(Producto.class)))
                .thenReturn(cuentaAhorros)
                .thenReturn(cuentaCorriente);
        when(transaccionRepository.save(any(Transaccion.class)))
                .thenReturn(debito)
                .thenReturn(credito);

        List<TransaccionDTO> resultado = transaccionService.realizarTransferencia(dto);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(transaccionRepository, times(2)).save(any(Transaccion.class));
        verify(productoRepository, times(2)).save(any(Producto.class));
    }

    @Test
    void cuandoTransferirAMismaCuenta_entoncesLanzaExcepcion() {
        TransferenciaDTO dto = new TransferenciaDTO();
        dto.setCuentaOrigenId(1L);
        dto.setCuentaDestinoId(1L);
        dto.setMonto(new BigDecimal("100000"));

        assertThrows(OperacionNoPermitidaException.class, () -> {
            transaccionService.realizarTransferencia(dto);
        });

        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }

    @Test
    void cuandoTransferenciaConSaldoInsuficiente_entoncesLanzaExcepcion() {
        TransferenciaDTO dto = new TransferenciaDTO();
        dto.setCuentaOrigenId(1L);
        dto.setCuentaDestinoId(2L);
        dto.setMonto(new BigDecimal("9999999"));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaAhorros));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(cuentaCorriente));

        assertThrows(SaldoInsuficienteException.class, () -> {
            transaccionService.realizarTransferencia(dto);
        });

        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }
}