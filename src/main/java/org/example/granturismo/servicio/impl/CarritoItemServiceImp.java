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

    @Override
    protected ICrudGenericoRepository<CarritoItem, Long> getRepo() {
        return repo;
    }

    @Override
    public CarritoItemDTO saveD(CarritoItemDTO.CarritoItemCADTO dto) {
        CarritoItem carritoItem = carritoItemMapper.toEntityFromCADTO(dto);

        Carrito carrito = carritoRepository.findById(dto.carrito())
                .orElseThrow(() -> new EntityNotFoundException("Carrito no encontrado"));

        carritoItem.setCarrito(carrito);
        CarritoItem carritoItemGuardado = repo.save(carritoItem);
        return carritoItemMapper.toDTO(carritoItemGuardado);
    }

    @Override
    public CarritoItemDTO updateD(CarritoItemDTO.CarritoItemCADTO dto, Long id) {
        CarritoItem carritoItem = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CarritoItem no encontrado"));

        CarritoItem carritoItemx = carritoItemMapper.toEntityFromCADTO(dto);
        carritoItemx.setIdCarritoItem(carritoItem.getIdCarritoItem());

        Carrito carrito = carritoRepository.findById(dto.carrito())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        carritoItemx.setCarrito(carrito);

        CarritoItem carritoItemActualizado = repo.save(carritoItemx);
        return carritoItemMapper.toDTO(carritoItemActualizado);
    }

    public Page<CarritoItem> listaPage(Pageable pageable){
        return repo.findAll(pageable);
    }
}
