package org.example.granturismo.servicio;

import org.example.granturismo.dtos.PaqueteDTO;
import org.example.granturismo.modelo.Paquete;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface IPaqueteService extends ICrudGenericoService<Paquete, Long> {

    PaqueteDTO saveD(PaqueteDTO.PaqueteCADTO dto, MultipartFile imagenFile) throws IOException;

    PaqueteDTO updateD(PaqueteDTO.PaqueteCADTO dto, Long id, MultipartFile imagenFile) throws IOException;

    Page<Paquete> listaPage(Pageable pageable);

    Paquete findById(Long id);
    List<Paquete> findAll();
    void delete(Long id);
}
