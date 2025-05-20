package org.example.granturismo.servicio;

import org.example.granturismo.dtos.ActividadDTO;
import org.example.granturismo.dtos.DestinoDTO;
import org.example.granturismo.dtos.PaqueteDTO;
import org.example.granturismo.modelo.Actividad;
import org.example.granturismo.modelo.Destino;
import org.example.granturismo.modelo.Paquete;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IDestinoService extends ICrudGenericoService<Destino, Long> {

    DestinoDTO saveD(DestinoDTO.DestinoCADTO dto, MultipartFile imagenFile) throws IOException;

    DestinoDTO updateD(DestinoDTO.DestinoCADTO dto, Long id, MultipartFile imagenFile) throws IOException;

    Page<Destino> listaPage(Pageable pageable);

    Destino findById(Long id);
    List<Destino> findAll();
    void delete(Long id);
}
