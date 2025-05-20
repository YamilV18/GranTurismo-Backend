package org.example.granturismo.servicio.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.ActividadDTO;
import org.example.granturismo.dtos.ActividadDetalleDTO;
import org.example.granturismo.dtos.PaqueteDTO;
import org.example.granturismo.dtos.PaqueteDetalleDTO;
import org.example.granturismo.mappers.ActividadDetalleMapper;
import org.example.granturismo.mappers.PaqueteDetalleMapper;
import org.example.granturismo.modelo.*;
import org.example.granturismo.repositorio.*;
import org.example.granturismo.servicio.IActividadDetalleService;
import org.example.granturismo.servicio.ICloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ActividadDetalleServiceImp extends CrudGenericoServiceImp<ActividadDetalle, Long> implements IActividadDetalleService {


    @Autowired
    private DataSource dataSource;

    private final IActividadDetalleRepository repo;
    private final ActividadDetalleMapper actividadDetalleMapper;
    private final IPaqueteRepository paqueteRepository;
    private final IActividadRepository actividadRepository;
    private final ICloudinaryService cloudinaryService;

    @Override
    protected ICrudGenericoRepository<ActividadDetalle, Long> getRepo() {
        return repo;
    }


    @Override
    public ActividadDetalleDTO saveD(ActividadDetalleDTO.ActividadDetalleCADTO dto, MultipartFile imagenFile) throws IOException {
        ActividadDetalle actividadDetalle = actividadDetalleMapper.toEntityFromCADTO(dto);

        Paquete paquete = paqueteRepository.findById(dto.paquete())
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado"));
        Actividad actividad = actividadRepository.findById(dto.actividad())
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada"));

        actividadDetalle.setPaquete(paquete);
        actividadDetalle.setActividad(actividad);

        // <-- Lógica para subir la imagen a Cloudinary -->
        if (imagenFile != null && !imagenFile.isEmpty()) {
            Map uploadResult = cloudinaryService.uploadFile(imagenFile); // Subir archivo y obtener resultado
            actividadDetalle.setImagenUrl((String) uploadResult.get("secure_url")); // Establecer la URL
            actividadDetalle.setImagenPublicId((String) uploadResult.get("public_id")); // Establecer el public_id (NUEVO)
        } else {
            throw new IllegalArgumentException("La imagen de la actividad es obligatoria.");
        }
        // <-- Fin lógica Cloudinary -->
        ActividadDetalle actividadDetalleGuardado = repo.save(actividadDetalle);
        return actividadDetalleMapper.toDTO(actividadDetalleGuardado);
    }

    @Override
    public ActividadDetalleDTO updateD(ActividadDetalleDTO.ActividadDetalleCADTO dto, Long id, MultipartFile imagenFile) throws IOException {
        ActividadDetalle actividadDetalleExistente = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Detalle de la Actividad no encontrado"));

        actividadDetalleExistente.setTitulo(dto.titulo());
        actividadDetalleExistente.setDescripcion(dto.descripcion());
        actividadDetalleExistente.setOrden(dto.orden());


        Paquete paquete = paqueteRepository.findById(dto.paquete())
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado"));
        Actividad actividad = actividadRepository.findById(dto.actividad())
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada"));

        actividadDetalleExistente.setPaquete(paquete);
        actividadDetalleExistente.setActividad(actividad);

        // <-- Lógica para actualizar la imagen con Cloudinary -->
        // Si se proporciona un nuevo archivo de imagen
        if (imagenFile != null && !imagenFile.isEmpty()) {
            // Opcional: Eliminar la imagen vieja de Cloudinary antes de subir la nueva
            String oldPublicId = actividadDetalleExistente.getImagenPublicId();
            if (oldPublicId != null && !oldPublicId.isEmpty()) {
                cloudinaryService.deleteFile(oldPublicId); // Llamar al método de eliminación
            }

            Map uploadResult = cloudinaryService.uploadFile(imagenFile); // Subir el nuevo archivo
            actividadDetalleExistente.setImagenUrl((String) uploadResult.get("secure_url")); // Establecer la nueva URL
            actividadDetalleExistente.setImagenPublicId((String) uploadResult.get("public_id")); // Establecer el nuevo public_id
        }
        // <-- Fin lógica Cloudinary -->

        ActividadDetalle actividadDetalleActualizado = repo.save(actividadDetalleExistente);
        return actividadDetalleMapper.toDTO(actividadDetalleActualizado);
    }

    // Implementación del método delete
    @Override
    public void delete(Long id) {
        ActividadDetalle actividadDetalleAEliminar = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Detalle de la Actividad no encontrada con id: " + id));

        // <-- Lógica para eliminar la imagen de Cloudinary -->
        String publicId = actividadDetalleAEliminar.getImagenPublicId();
        if (publicId != null && !publicId.isEmpty()) {
            try {
                cloudinaryService.deleteFile(publicId); // Llamar al método de eliminación
            } catch (IOException e) {
                System.err.println("Error al eliminar la imagen de Cloudinary para el Detalle de la Actividad con ID " + id + " y publicId " + publicId + ": " + e.getMessage());
            }
        }
        // <-- Fin lógica Cloudinary -->

        // Eliminar el paquete de la base de datos
        repo.delete(actividadDetalleAEliminar);
    }

    @Override
    public ActividadDetalle findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Detalle de la Actividad no encontrada con id: " + id));
    }

    @Override
    public List<ActividadDetalle> findAll() {
        return repo.findAll();
    }


    public Page<ActividadDetalle> listaPage(Pageable pageable){
        return repo.findAll(pageable);
    }
}
