package com.financiera.backend.service;

import com.financiera.backend.dto.ClienteDTO;
import com.financiera.backend.entity.Cliente;
import com.financiera.backend.exception.*;
import com.financiera.backend.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    // Crear cliente
    @Transactional
    public ClienteDTO crearCliente(ClienteDTO clienteDTO) {

        validarMayorDeEdad(clienteDTO.getFechaNacimiento());


        if (clienteRepository.existsByNumeroIdentificacion(clienteDTO.getNumeroIdentificacion())) {
            throw new DatoDuplicadoException(
                    "Ya existe un cliente con el número de identificación: " + clienteDTO.getNumeroIdentificacion()
            );
        }


        if (clienteRepository.existsByCorreoElectronico(clienteDTO.getCorreoElectronico())) {
            throw new DatoDuplicadoException(
                    "Ya existe un cliente con el correo: " + clienteDTO.getCorreoElectronico()
            );
        }


        Cliente cliente = convertirDTOaEntity(clienteDTO);


        Cliente clienteGuardado = clienteRepository.save(cliente);


        return convertirEntityADTO(clienteGuardado);
    }

    // Obtener todos los clientes
    @Transactional(readOnly = true)
    public List<ClienteDTO> obtenerTodosLosClientes() {
        return clienteRepository.findAll()
                .stream()
                .map(this::convertirEntityADTO)
                .collect(Collectors.toList());
    }

    // Obtener cliente por ID
    @Transactional(readOnly = true)
    public ClienteDTO obtenerClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el cliente con ID: " + id
                ));
        return convertirEntityADTO(cliente);
    }

    // Actualizar cliente
    @Transactional
    public ClienteDTO actualizarCliente(Long id, ClienteDTO clienteDTO) {

        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el cliente con ID: " + id
                ));


        validarMayorDeEdad(clienteDTO.getFechaNacimiento());

        // Validar que no exista otro cliente con el mismo número de identificación
        if (!clienteExistente.getNumeroIdentificacion().equals(clienteDTO.getNumeroIdentificacion())) {
            if (clienteRepository.existsByNumeroIdentificacion(clienteDTO.getNumeroIdentificacion())) {
                throw new DatoDuplicadoException(
                        "Ya existe otro cliente con el número de identificación: " + clienteDTO.getNumeroIdentificacion()
                );
            }
        }


        if (!clienteExistente.getCorreoElectronico().equals(clienteDTO.getCorreoElectronico())) {
            if (clienteRepository.existsByCorreoElectronico(clienteDTO.getCorreoElectronico())) {
                throw new DatoDuplicadoException(
                        "Ya existe otro cliente con el correo: " + clienteDTO.getCorreoElectronico()
                );
            }
        }


        clienteExistente.setTipoIdentificacion(clienteDTO.getTipoIdentificacion());
        clienteExistente.setNumeroIdentificacion(clienteDTO.getNumeroIdentificacion());
        clienteExistente.setNombres(clienteDTO.getNombres());
        clienteExistente.setApellido(clienteDTO.getApellido());
        clienteExistente.setCorreoElectronico(clienteDTO.getCorreoElectronico());
        clienteExistente.setFechaNacimiento(clienteDTO.getFechaNacimiento());



        Cliente clienteActualizado = clienteRepository.save(clienteExistente);

        return convertirEntityADTO(clienteActualizado);
    }

    // Eliminar cliente
    @Transactional
    public void eliminarCliente(Long id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el cliente con ID: " + id
                ));


        clienteRepository.delete(cliente);
    }

    // Metodos auxiliares


    private void validarMayorDeEdad(LocalDate fechaNacimiento) {
        LocalDate hoy = LocalDate.now();
        int edad = Period.between(fechaNacimiento, hoy).getYears();

        if (edad < 18) {
            throw new ClienteMenorDeEdadException(
                    "El cliente debe ser mayor de edad. Edad actual: " + edad + " años"
            );
        }
    }


    private ClienteDTO convertirEntityADTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setTipoIdentificacion(cliente.getTipoIdentificacion());
        dto.setNumeroIdentificacion(cliente.getNumeroIdentificacion());
        dto.setNombres(cliente.getNombres());
        dto.setApellido(cliente.getApellido());
        dto.setCorreoElectronico(cliente.getCorreoElectronico());
        dto.setFechaNacimiento(cliente.getFechaNacimiento());
        dto.setFechaCreacion(cliente.getFechaCreacion());
        dto.setFechaModificacion(cliente.getFechaModificacion());
        return dto;
    }


    private Cliente convertirDTOaEntity(ClienteDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setId(dto.getId());
        cliente.setTipoIdentificacion(dto.getTipoIdentificacion());
        cliente.setNumeroIdentificacion(dto.getNumeroIdentificacion());
        cliente.setNombres(dto.getNombres());
        cliente.setApellido(dto.getApellido());
        cliente.setCorreoElectronico(dto.getCorreoElectronico());
        cliente.setFechaNacimiento(dto.getFechaNacimiento());
        return cliente;
    }
}