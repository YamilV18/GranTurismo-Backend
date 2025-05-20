package org.example.granturismo.servicio;

import org.example.granturismo.dtos.ActividadDetalleDTO;
import org.example.granturismo.modelo.ActividadDetalle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IActividadDetalleService extends ICrudGenericoService<ActividadDetalle, Long> {

    ActividadDetalleDTO saveD(ActividadDetalleDTO.ActividadDetalleCADTO dto, MultipartFile imagenFile) throws IOException;

    ActividadDetalleDTO updateD(ActividadDetalleDTO.ActividadDetalleCADTO dto, Long id, MultipartFile imagenFile) throws IOException;

    Page<ActividadDetalle> listaPage(Pageable pageable);

    ActividadDetalle findById(Long id);
    List<ActividadDetalle> findAll();
    void delete(Long id);
}
