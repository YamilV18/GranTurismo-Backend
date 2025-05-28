package org.example.granturismo.servicio.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.CarritoItemDTO;
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
    public CarritoItemDTO updateD(CarritoItemDTO.CarritoItemCADTO dto, Long id) {
        CarritoItem carritoItem = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada"));

        CarritoItem carritoitemx = carritoItemMapper.toEntityFromCADTO(dto);
        carritoitemx.setIdCarritoItem(carritoItem.getIdCarritoItem());

        Carrito carrito = carritoRepository.findById(dto.carrito())
                .orElseThrow(() -> new EntityNotFoundException("Carrito no encontrado"));
        Servicio servicio = servicioRepository.findById(dto.servicio())
                .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado"));
        Actividad actividad = actividadRepository.findById(dto.actividad())
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada"));


        carritoitemx.setCarrito(carrito);
        carritoitemx.setServicio(servicio);
        carritoitemx.setActividad(actividad);

        CarritoItem carritoitemActualizado = repo.save(carritoitemx);
        return carritoItemMapper.toDTO(carritoitemActualizado);
    }

    public Page<CarritoItem> listaPage(Pageable pageable){
        return repo.findAll(pageable);
    }
}
