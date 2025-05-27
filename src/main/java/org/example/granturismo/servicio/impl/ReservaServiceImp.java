package org.example.granturismo.servicio.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.ReservaDTO;
import org.example.granturismo.mappers.ReservaMapper;
import org.example.granturismo.modelo.Paquete;
import org.example.granturismo.modelo.Reserva;
import org.example.granturismo.modelo.Usuario;
import org.example.granturismo.repositorio.*;
import org.example.granturismo.servicio.IReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservaServiceImp extends CrudGenericoServiceImp<Reserva, Long> implements IReservaService {

    @Autowired
    private DataSource dataSource;

    private final IReservaRepository repo;
    private final ReservaMapper reservaMapper;
    private final IUsuarioRepository usuarioRepository;
    private final IPaqueteRepository paqueteRepository;

    @Override
    protected ICrudGenericoRepository<Reserva, Long> getRepo() {
        return repo;
    }

    @Override
    public ReservaDTO saveD(ReservaDTO.ReservaCADTO dto) {
        Reserva reserva = reservaMapper.toEntityFromCADTO(dto);

        Usuario usuario = usuarioRepository.findById(dto.usuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        Paquete paquete = paqueteRepository.findById(dto.paquete())
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado"));

        reserva.setUsuario(usuario);
        reserva.setPaquete(paquete);

        Reserva reservaGuardado = repo.save(reserva);
        return reservaMapper.toDTO(reservaGuardado);
    }

    @Override
    public ReservaDTO updateD(ReservaDTO.ReservaCADTO dto, Long id) {
        Reserva reserva = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada"));

        Reserva reservax = reservaMapper.toEntityFromCADTO(dto);
        reservax.setIdReserva(reserva.getIdReserva());

        Usuario usuario = usuarioRepository.findById(dto.usuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        Paquete paquete = paqueteRepository.findById(dto.paquete())
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado"));

        reservax.setUsuario(usuario);
        reservax.setPaquete(paquete);

        Reserva reservaActualizado = repo.save(reservax);
        return reservaMapper.toDTO(reservaActualizado);
    }

    public Page<Reserva> listaPage(Pageable pageable){
        return repo.findAll(pageable);
    }
}

