package org.example.granturismo.servicio;

import org.example.granturismo.dtos.ActividadDTO;
import org.example.granturismo.dtos.PaqueteDTO;
import org.example.granturismo.modelo.Actividad;
import org.example.granturismo.modelo.Paquete;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IActividadService extends ICrudGenericoService<Actividad, Long> {

    ActividadDTO saveD(ActividadDTO.ActividadCADTO dto, MultipartFile imagenFile) throws IOException;

    ActividadDTO updateD(ActividadDTO.ActividadCADTO dto, Long id, MultipartFile imagenFile) throws IOException;

    Page<Actividad> listaPage(Pageable pageable);

    Actividad findById(Long id);
    List<Actividad> findAll();
    void delete(Long id);
}
