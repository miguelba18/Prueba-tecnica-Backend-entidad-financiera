package com.financiera.backend.service;

import com.financiera.backend.dto.ProductoDTO;
import com.financiera.backend.entity.Cliente;
import com.financiera.backend.entity.Producto;
import com.financiera.backend.exception.productos.CuentaNoPuedeCancelarseException;
import com.financiera.backend.exception.clientes.RecursoNoEncontradoException;
import com.financiera.backend.exception.productos.OperacionNoPermitidaException;
import com.financiera.backend.repository.ClienteRepository;
import com.financiera.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;

    // Crear producto (cuenta)
    @Transactional
    public ProductoDTO crearProducto(ProductoDTO productoDTO) {
        // Validar que el cliente existe
        Cliente cliente = clienteRepository.findById(productoDTO.getClienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el cliente con ID: " + productoDTO.getClienteId()
                ));


        Producto producto = new Producto();
        producto.setTipoCuenta(productoDTO.getTipoCuenta());
        producto.setCliente(cliente);


        producto.setNumeroCuenta(generarNumeroCuenta(productoDTO.getTipoCuenta()));

        producto.setEstado(Producto.EstadoCuenta.ACTIVA);

        producto.setSaldo(BigDecimal.ZERO);

        producto.setExentaGMF(productoDTO.getExentaGMF() != null ? productoDTO.getExentaGMF() : false);


        Producto productoGuardado = productoRepository.save(producto);

        return convertirEntityADTO(productoGuardado);
    }

    // Obtener todos los productos
    @Transactional(readOnly = true)
    public List<ProductoDTO> obtenerTodosLosProductos() {
        return productoRepository.findAll()
                .stream()
                .map(this::convertirEntityADTO)
                .collect(Collectors.toList());
    }

    // Obtener producto por ID
    @Transactional(readOnly = true)
    public ProductoDTO obtenerProductoPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el producto con ID: " + id
                ));
        return convertirEntityADTO(producto);
    }

    // Obtener productos de un cliente
    @Transactional(readOnly = true)
    public List<ProductoDTO> obtenerProductosPorCliente(Long clienteId) {

        if (!clienteRepository.existsById(clienteId)) {
            throw new RecursoNoEncontradoException(
                    "No se encontró el cliente con ID: " + clienteId
            );
        }

        return productoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::convertirEntityADTO)
                .collect(Collectors.toList());
    }

    // Actualizar estado de cuenta (activar/inactivar)
    @Transactional
    public ProductoDTO actualizarEstado(Long id, Producto.EstadoCuenta nuevoEstado) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el producto con ID: " + id
                ));


        if (nuevoEstado == Producto.EstadoCuenta.CANCELADA) {
            if (producto.getSaldo().compareTo(BigDecimal.ZERO) != 0) {
                throw new CuentaNoPuedeCancelarseException(
                        "No se puede cancelar la cuenta. El saldo debe ser $0. Saldo actual: $" + producto.getSaldo()
                );
            }
        }

        producto.setEstado(nuevoEstado);
        Producto productoActualizado = productoRepository.save(producto);

        return convertirEntityADTO(productoActualizado);
    }

    // Eliminar producto
    @Transactional
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el producto con ID: " + id
                ));


        if (producto.getSaldo().compareTo(BigDecimal.ZERO) != 0) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar la cuenta. El saldo debe ser $0. Saldo actual: $" + producto.getSaldo()
            );
        }

        productoRepository.delete(producto);
    }

    // ========== MÉTODOS AUXILIARES ==========


    private String generarNumeroCuenta(Producto.TipoCuenta tipoCuenta) {
        String prefijo = tipoCuenta == Producto.TipoCuenta.CUENTA_AHORROS ? "53" : "33";
        String numeroCuenta;
        Random random = new Random();

        do {

            StringBuilder sb = new StringBuilder(prefijo);
            for (int i = 0; i < 8; i++) {
                sb.append(random.nextInt(10));
            }
            numeroCuenta = sb.toString();
        } while (productoRepository.existsByNumeroCuenta(numeroCuenta));

        return numeroCuenta;
    }


    private ProductoDTO convertirEntityADTO(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setTipoCuenta(producto.getTipoCuenta());
        dto.setNumeroCuenta(producto.getNumeroCuenta());
        dto.setEstado(producto.getEstado());
        dto.setSaldo(producto.getSaldo());
        dto.setExentaGMF(producto.getExentaGMF());
        dto.setFechaCreacion(producto.getFechaCreacion());
        dto.setFechaModificacion(producto.getFechaModificacion());
        dto.setClienteId(producto.getCliente().getId());
        dto.setNombreCliente(producto.getCliente().getNombres() + " " + producto.getCliente().getApellido());
        return dto;
    }
}