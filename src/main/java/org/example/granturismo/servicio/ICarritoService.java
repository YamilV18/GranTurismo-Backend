package org.example.granturismo.servicio;

import org.eclipse.jdt.internal.compiler.env.IModule;
import org.example.granturismo.dtos.CarritoDTO;
import org.example.granturismo.dtos.ReservaDTO;
import org.example.granturismo.modelo.Carrito;
import org.example.granturismo.modelo.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public interface ICarritoService extends ICrudGenericoService<Carrito, Long> {

    CarritoDTO saveD(CarritoDTO.CarritoCADTO dto);

    CarritoDTO updateD(CarritoDTO.CarritoCADTO dto, Long id);

    Optional<Carrito> findByUsuario(Long usuarioId);

    Page<Carrito> listaPage(Pageable pageable);
}
