package org.example.granturismo.servicio;

import org.example.granturismo.dtos.LocalizedResponseDto;
import org.example.granturismo.dtos.PaqueteDTO;
import org.example.granturismo.modelo.Paquete;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public interface IPaqueteService extends ICrudGenericoService<Paquete, Long> {

    PaqueteDTO saveD(PaqueteDTO.PaqueteCADTO dto, MultipartFile imagenFile) throws IOException;

    PaqueteDTO updateD(PaqueteDTO.PaqueteCADTO dto, Long id, MultipartFile imagenFile) throws IOException;

    Page<Paquete> listaPage(Pageable pageable);

    Paquete findById(Long id);
    List<Paquete> findAll();
    void delete(Long id);

    // NUEVOS MÉTODOS CON LOCALIZACIÓN

    /**
     * Obtiene un paquete por ID, localizado según las preferencias del usuario
     */
    LocalizedResponseDto<PaqueteDTO> getPaqueteLocalizado(Long id, Long userId);

    /**
     * Obtiene una lista paginada de paquetes localizados
     */
    Page<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>> getPaquetesLocalizados(Long userId, Pageable pageable);

    /**
     * Busca paquetes por estado, localizados
     */
    List<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>> buscarPorEstadoLocalizado(Paquete.Estado estado, Long userId); // <--- This line is the key

    /**
     * Obtiene un paquete sin localización (para administradores)
     */
    PaqueteDTO getPaqueteOriginal(Long id);

    /**
     * Obtiene estadísticas de localización para un paquete
     */
    Map<String, Object> getEstadisticasLocalizacion(Long id, Long userId);



}
