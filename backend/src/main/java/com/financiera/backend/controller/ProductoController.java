package com.financiera.backend.controller;

import com.financiera.backend.dto.ProductoDTO;
import com.financiera.backend.entity.Producto;
import com.financiera.backend.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;


    @PostMapping
    public ResponseEntity<ProductoDTO> crearProducto(@Valid @RequestBody ProductoDTO productoDTO) {
        ProductoDTO productoCreado = productoService.crearProducto(productoDTO);
        return new ResponseEntity<>(productoCreado, HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<List<ProductoDTO>> obtenerTodosLosProductos() {
        List<ProductoDTO> productos = productoService.obtenerTodosLosProductos();
        return ResponseEntity.ok(productos);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> obtenerProductoPorId(@PathVariable Long id) {
        ProductoDTO producto = productoService.obtenerProductoPorId(id);
        return ResponseEntity.ok(producto);
    }


    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ProductoDTO>> obtenerProductosPorCliente(@PathVariable Long clienteId) {
        List<ProductoDTO> productos = productoService.obtenerProductosPorCliente(clienteId);
        return ResponseEntity.ok(productos);
    }


    @PatchMapping("/{id}/estado")
    public ResponseEntity<ProductoDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestParam Producto.EstadoCuenta estado) {
        ProductoDTO productoActualizado = productoService.actualizarEstado(id, estado);
        return ResponseEntity.ok(productoActualizado);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}