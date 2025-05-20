package org.example.granturismo.servicio.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.DestinoDTO;
import org.example.granturismo.dtos.PaqueteDTO;
import org.example.granturismo.mappers.DestinoMapper;
import org.example.granturismo.mappers.PaqueteMapper;
import org.example.granturismo.modelo.Actividad;
import org.example.granturismo.modelo.Destino;
import org.example.granturismo.modelo.Paquete;
import org.example.granturismo.modelo.Proveedor;
import org.example.granturismo.repositorio.ICrudGenericoRepository;
import org.example.granturismo.repositorio.IDestinoRepository;
import org.example.granturismo.repositorio.IPaqueteRepository;
import org.example.granturismo.repositorio.IProveedorRepository;
import org.example.granturismo.servicio.ICloudinaryService;
import org.example.granturismo.servicio.IDestinoService;
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

public class DestinoServiceImp extends CrudGenericoServiceImp<Destino, Long> implements IDestinoService {

    @Autowired
    private DataSource dataSource;

    private final IDestinoRepository repo;
    private final DestinoMapper destinoMapper;
    private final ICloudinaryService cloudinaryService;

    @Override
    protected ICrudGenericoRepository<Destino, Long> getRepo() {
        return repo;
    }

    @Override
    public DestinoDTO saveD(DestinoDTO.DestinoCADTO dto, MultipartFile imagenFile) throws IOException {
        Destino destino = destinoMapper.toEntityFromCADTO(dto);

        // <-- Lógica para subir la imagen a Cloudinary -->
        if (imagenFile != null && !imagenFile.isEmpty()) {
            Map uploadResult = cloudinaryService.uploadFile(imagenFile); // Subir archivo y obtener resultado
            destino.setImagenUrl((String) uploadResult.get("secure_url")); // Establecer la URL
            destino.setImagenPublicId((String) uploadResult.get("public_id")); // Establecer el public_id (NUEVO)
        } else {
            throw new IllegalArgumentException("La imagen del Destino es obligatoria.");
        }
        // <-- Fin lógica Cloudinary -->

        Destino destinoGuardado = repo.save(destino);
        return destinoMapper.toDTO(destinoGuardado);
    }

    @Override
    public DestinoDTO updateD(DestinoDTO.DestinoCADTO dto,  Long id, MultipartFile imagenFile) throws IOException {
        Destino destinoExistente = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Destino no encontrado"));

        destinoExistente.setNombre(dto.nombre());
        destinoExistente.setDescripcion(dto.descripcion());
        destinoExistente.setUbicacion(dto.ubicacion());
        destinoExistente.setLatitud(dto.latitud());
        destinoExistente.setLongitud(dto.longitud());
        destinoExistente.setPopularidad(dto.popularidad());
        destinoExistente.setPreciomedio(dto.preciomedio());
        destinoExistente.setRating(dto.rating());

        // <-- Lógica para actualizar la imagen con Cloudinary -->
        // Si se proporciona un nuevo archivo de imagen
        if (imagenFile != null && !imagenFile.isEmpty()) {
            // Opcional: Eliminar la imagen vieja de Cloudinary antes de subir la nueva
            String oldPublicId = destinoExistente.getImagenPublicId();
            if (oldPublicId != null && !oldPublicId.isEmpty()) {
                cloudinaryService.deleteFile(oldPublicId); // Llamar al método de eliminación
            }

            Map uploadResult = cloudinaryService.uploadFile(imagenFile); // Subir el nuevo archivo
            destinoExistente.setImagenUrl((String) uploadResult.get("secure_url")); // Establecer la nueva URL
            destinoExistente.setImagenPublicId((String) uploadResult.get("public_id")); // Establecer el nuevo public_id
        }
        Destino destinoActualizado = repo.save(destinoExistente);
        return destinoMapper.toDTO(destinoActualizado);
    }
    // Implementación del método delete
    @Override
    public void delete(Long id) {
        Destino destinoAEliminar = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Destino no encontrado con id: " + id));

        // <-- Lógica para eliminar la imagen de Cloudinary -->
        String publicId = destinoAEliminar.getImagenPublicId();
        if (publicId != null && !publicId.isEmpty()) {
            try {
                cloudinaryService.deleteFile(publicId); // Llamar al método de eliminación
            } catch (IOException e) {
                System.err.println("Error al eliminar la imagen de Cloudinary para el Destino con ID " + id + " y publicId " + publicId + ": " + e.getMessage());
            }
        }
        // <-- Fin lógica Cloudinary -->
        repo.delete(destinoAEliminar);
    }

    @Override
    public Destino findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Destino no encontrado con id: " + id));
    }

    @Override
    public List<Destino> findAll() {
        return repo.findAll();
    }

    public Page<Destino> listaPage(Pageable pageable){
        return repo.findAll(pageable);
    }
}
