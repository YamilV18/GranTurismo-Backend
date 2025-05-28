package org.example.granturismo.servicio.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.CarritoDTO;
import org.example.granturismo.dtos.CarritoItemDTO;
import org.example.granturismo.dtos.ReservaDTO;
import org.example.granturismo.mappers.CarritoItemMapper;
import org.example.granturismo.modelo.*;
import org.example.granturismo.repositorio.*;
import org.example.granturismo.servicio.ICarritoItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Service
@Transactional
@RequiredArgsConstructor

public class CarritoItemServiceImp extends CrudGenericoServiceImp<CarritoItem, Long> implements ICarritoItemService {

    @Autowired
    private DataSource dataSource;

    private final ICarritoItemRepository repo;
    private final CarritoItemMapper carritoItemMapper;
    private final ICarritoRepository carritoRepository;
    private final IServicioRepository servicioRepository;
    private final IActividadRepository actividadRepository;

    @Override
    protected ICrudGenericoRepository<CarritoItem, Long> getRepo() {
        return repo;
    }

    @Override
    public CarritoItemDTO saveD(CarritoItemDTO.CarritoItemCADTO dto) { // Changed input DTO
        CarritoItem carritoitem = carritoItemMapper.toEntityFromCADTO(dto);

        Carrito carrito = carritoRepository.findById(dto.carrito())
                .orElseThrow(() -> new EntityNotFoundException("Carrito no encontrado"));
        Servicio servicio = servicioRepository.findById(dto.servicio())
                .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado"));
        Actividad actividad = actividadRepository.findById(dto.actividad())
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada"));


        carritoitem.setCarrito(carrito);
        carritoitem.setServicio(servicio);
        carritoitem.setActividad(actividad);


        CarritoItem carritoitemGuardado = repo.save(carritoitem); // Corrected type
        return carritoItemMapper.toDTO(carritoitemGuardado); // Should map CarritoItem to CarritoItemDTO
    }

    @Override
    public CarritoItemDTO updateD(CarritoItemDTO.CarritoItemCADTO dto, Long id) { // Changed input and return DTOs
        CarritoItem carritoItemExistente = repo.findById(id) // Get the existing CarritoItem
                .orElseThrow(() -> new EntityNotFoundException("CarritoItem no encontrado"));

        Carrito carrito = carritoRepository.findById(dto.carrito())
                .orElseThrow(() -> new EntityNotFoundException("Carrito no encontrado"));
        Servicio servicio = servicioRepository.findById(dto.servicio())
                .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado"));
        Actividad actividad = actividadRepository.findById(dto.actividad())
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada"));


        carritoItemExistente.setCarrito(carrito);
        carritoItemExistente.setServicio(servicio);
        carritoItemExistente.setActividad(actividad);


        // Save the updated existing entity
        CarritoItem carritoItemActualizado = repo.save(carritoItemExistente);
        return carritoItemMapper.toDTO(carritoItemActualizado); // Map the updated CarritoItem to CarritoItemDTO
    }

    public Page<CarritoItem> listaPage(Pageable pageable){
        return repo.findAll(pageable);
    }
}
