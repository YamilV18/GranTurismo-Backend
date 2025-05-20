package org.example.granturismo.servicio.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.ActividadDTO;
import org.example.granturismo.mappers.ActividadMapper;
import org.example.granturismo.modelo.Actividad;
import org.example.granturismo.repositorio.IActividadRepository;
import org.example.granturismo.repositorio.ICrudGenericoRepository;
import org.example.granturismo.servicio.IActividadService;
import org.example.granturismo.servicio.ICloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ActividadServiceImp extends CrudGenericoServiceImp<Actividad, Long> implements IActividadService {

    @Autowired
    private DataSource dataSource;

    private final IActividadRepository repo;
    private final ActividadMapper actividadMapper;
    private final ICloudinaryService cloudinaryService;

    @Override
    protected ICrudGenericoRepository<Actividad, Long> getRepo() {
        return repo;
    }

    @Override
    public ActividadDTO saveD(ActividadDTO.ActividadCADTO dto, MultipartFile imagenFile) throws IOException {

        Actividad actividad = actividadMapper.toEntityFromCADTO(dto);

        // <-- Lógica para subir la imagen a Cloudinary -->
        if (imagenFile != null && !imagenFile.isEmpty()) {
            Map uploadResult = cloudinaryService.uploadFile(imagenFile); // Subir archivo y obtener resultado
            actividad.setImagenUrl((String) uploadResult.get("secure_url")); // Establecer la URL
            actividad.setImagenPublicId((String) uploadResult.get("public_id")); // Establecer el public_id (NUEVO)
        } else {
            throw new IllegalArgumentException("La imagen de la actividad es obligatoria.");
        }
        // <-- Fin lógica Cloudinary -->

        Actividad actividadGuardado = repo.save(actividad);
        return actividadMapper.toDTO(actividadGuardado);
    }

    @Override
    public ActividadDTO updateD(ActividadDTO.ActividadCADTO dto, Long id, MultipartFile imagenFile) throws IOException {
        Actividad actividadExistente = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada"));

        actividadExistente.setTitulo(dto.titulo());
        actividadExistente.setDescripcion(dto.descripcion());
        actividadExistente.setTipo(dto.tipo());
        actividadExistente.setDuracionHoras(dto.duracionHoras());
        actividadExistente.setPrecioBase(dto.precioBase());

        // <-- Lógica para actualizar la imagen con Cloudinary -->
        // Si se proporciona un nuevo archivo de imagen
        if (imagenFile != null && !imagenFile.isEmpty()) {
            // Opcional: Eliminar la imagen vieja de Cloudinary antes de subir la nueva
            String oldPublicId = actividadExistente.getImagenPublicId();
            if (oldPublicId != null && !oldPublicId.isEmpty()) {
                cloudinaryService.deleteFile(oldPublicId); // Llamar al método de eliminación
            }

            Map uploadResult = cloudinaryService.uploadFile(imagenFile); // Subir el nuevo archivo
            actividadExistente.setImagenUrl((String) uploadResult.get("secure_url")); // Establecer la nueva URL
            actividadExistente.setImagenPublicId((String) uploadResult.get("public_id")); // Establecer el nuevo public_id
        }
        // <-- Fin lógica Cloudinary -->

        Actividad actividadActualizada = repo.save(actividadExistente);
        return actividadMapper.toDTO(actividadActualizada);
    }
    // Implementación del método delete
    @Override
    public void delete(Long id) {
        Actividad actividadAEliminar = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrado con id: " + id));

        // <-- Lógica para eliminar la imagen de Cloudinary -->
        String publicId = actividadAEliminar.getImagenPublicId();
        if (publicId != null && !publicId.isEmpty()) {
            try {
                cloudinaryService.deleteFile(publicId); // Llamar al método de eliminación
            } catch (IOException e) {
                System.err.println("Error al eliminar la imagen de Cloudinary para la Actividad con ID " + id + " y publicId " + publicId + ": " + e.getMessage());
            }
        }
        // <-- Fin lógica Cloudinary -->

        // Eliminar la actividad de la base de datos
        repo.delete(actividadAEliminar);
    }
    @Override
    public Actividad findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado con id: " + id));
    }

    @Override
    public List<Actividad> findAll() {
        return repo.findAll();
    }

    public Page<Actividad> listaPage(Pageable pageable){
        return repo.findAll(pageable);
    }
}
