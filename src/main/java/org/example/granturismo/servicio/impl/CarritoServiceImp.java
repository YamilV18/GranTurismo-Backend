package org.example.granturismo.servicio.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.CarritoDTO;
import org.example.granturismo.mappers.CarritoMapper;
import org.example.granturismo.modelo.Carrito;

import org.example.granturismo.modelo.Favorito;
import org.example.granturismo.modelo.Usuario;
import org.example.granturismo.repositorio.*;
import org.example.granturismo.servicio.ICarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CarritoServiceImp extends CrudGenericoServiceImp<Carrito, Long> implements ICarritoService {
    @Autowired
    private DataSource dataSource;

    private final ICarritoRepository repo;
    private final CarritoMapper carritoMapper;
    private final IUsuarioRepository usuarioRepository;

    @Override
    protected ICrudGenericoRepository<Carrito, Long> getRepo() {
        return repo;
    }

    @Override
    public CarritoDTO saveD(CarritoDTO.CarritoCADTO dto) {
        Carrito carrito = carritoMapper.toEntityFromCADTO(dto);

        Usuario usuario = usuarioRepository.findById(dto.usuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        carrito.setUsuario(usuario);

        Carrito carritoGuardado = repo.save(carrito);
        return carritoMapper.toDTO(carritoGuardado);
    }

    @Override
    public CarritoDTO updateD(CarritoDTO.CarritoCADTO dto, Long id) {
        Carrito carrito = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Carrito no encontrada"));

        Carrito carritox = carritoMapper.toEntityFromCADTO(dto);
        carritox.setIdCarrito(carrito.getIdCarrito());

        Usuario usuario = usuarioRepository.findById(dto.usuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        carritox.setUsuario(usuario);

        Carrito carritoActualizado = repo.save(carritox);
        return carritoMapper.toDTO(carritoActualizado);
    }

    @Override
    public Optional<Carrito> findByUsuario(Long usuarioId) {
        return Optional.ofNullable(repo.findByUsuario_IdUsuario(usuarioId).orElseThrow(() -> new EntityNotFoundException("Carrito no encontrado")));
    }

    public Page<Carrito> listaPage(Pageable pageable){
        return repo.findAll(pageable);
    }
}
