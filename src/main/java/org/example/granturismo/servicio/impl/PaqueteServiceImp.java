package org.example.granturismo.servicio.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import org.example.granturismo.dtos.PaqueteDTO;
import org.example.granturismo.mappers.PaqueteMapper;
import org.example.granturismo.modelo.Destino;
import org.example.granturismo.modelo.Paquete;
import org.example.granturismo.modelo.Proveedor;
import org.example.granturismo.modelo.Rol;
import org.example.granturismo.repositorio.ICrudGenericoRepository;
import org.example.granturismo.repositorio.IPaqueteRepository;
import org.example.granturismo.repositorio.IProveedorRepository;
import org.example.granturismo.servicio.ICloudinaryService;
import org.example.granturismo.servicio.IPaqueteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.granturismo.repositorio.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor

public class PaqueteServiceImp extends CrudGenericoServiceImp<Paquete, Long> implements IPaqueteService {

    @Autowired
    private DataSource dataSource;

    private final IPaqueteRepository repo;
    private final PaqueteMapper paqueteMapper;
    private final IProveedorRepository proveedorRepository;
    private final IDestinoRepository destinoRepository;
    private final ICloudinaryService cloudinaryService;

    @Override
    protected ICrudGenericoRepository<Paquete, Long> getRepo() {
        return repo;
    }


    @Override
    public PaqueteDTO saveD(PaqueteDTO.PaqueteCADTO dto, MultipartFile imagenFile) throws IOException {
        // Mapear DTO a entidad (sin imagenUrl ni imagenPublicId aún)
        Paquete paquete = paqueteMapper.toEntityFromCADTO(dto);

        Proveedor proveedor = proveedorRepository.findById(dto.proveedor())
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));
        Destino destino = destinoRepository.findById(dto.destino())
                .orElseThrow(() -> new EntityNotFoundException("Destino no encontrado"));

        paquete.setProveedor(proveedor);
        paquete.setDestino(destino);

        // <-- Lógica para subir la imagen a Cloudinary -->
        if (imagenFile != null && !imagenFile.isEmpty()) {
            Map uploadResult = cloudinaryService.uploadFile(imagenFile); // Subir archivo y obtener resultado
            paquete.setImagenUrl((String) uploadResult.get("secure_url")); // Establecer la URL
            paquete.setImagenPublicId((String) uploadResult.get("public_id")); // Establecer el public_id (NUEVO)
        } else {
            // Si la imagen es obligatoria según tu modelo Paquete (nullable = false en imagen_url)
            // entonces debes lanzar una excepción aquí si no se proporciona archivo.
            // Tu modelo actual marca imagen_url como nullable = false, así que es obligatoria.
            throw new IllegalArgumentException("La imagen del paquete es obligatoria.");
        }
        // <-- Fin lógica Cloudinary -->

        Paquete paqueteGuardado = repo.save(paquete);
        return paqueteMapper.toDTO(paqueteGuardado);
    }

    @Override
    public PaqueteDTO updateD(PaqueteDTO.PaqueteCADTO dto, Long id, MultipartFile imagenFile) throws IOException {
        Paquete paqueteExistente  = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado"));

        // Actualizar campos del paqueteExistente con los datos del DTO
        paqueteExistente.setTitulo(dto.titulo());
        paqueteExistente.setDescripcion(dto.descripcion());
        // ... actualizar otros campos del PaqueteExistente con los valores de dto ...
        paqueteExistente.setPrecioTotal(dto.precioTotal());
        paqueteExistente.setEstado(dto.estado());
        paqueteExistente.setDuracionDias(dto.duracionDias());
        paqueteExistente.setLocalidad(dto.localidad());
        paqueteExistente.setTipoActividad(dto.tipoActividad());
        paqueteExistente.setCuposMaximos(dto.cuposMaximos());
        paqueteExistente.setFechaInicio(dto.fechaInicio());
        paqueteExistente.setFechaFin(dto.fechaFin());

        Proveedor proveedor = proveedorRepository.findById(dto.proveedor())
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));
        Destino destino = destinoRepository.findById(dto.destino())
                .orElseThrow(() -> new EntityNotFoundException("Destino no encontrado"));

        paqueteExistente.setProveedor(proveedor);
        paqueteExistente.setDestino(destino);

        // <-- Lógica para actualizar la imagen con Cloudinary -->
        // Si se proporciona un nuevo archivo de imagen
        if (imagenFile != null && !imagenFile.isEmpty()) {
            // Opcional: Eliminar la imagen vieja de Cloudinary antes de subir la nueva
            String oldPublicId = paqueteExistente.getImagenPublicId();
            if (oldPublicId != null && !oldPublicId.isEmpty()) {
                cloudinaryService.deleteFile(oldPublicId); // Llamar al método de eliminación
            }

            Map uploadResult = cloudinaryService.uploadFile(imagenFile); // Subir el nuevo archivo
            paqueteExistente.setImagenUrl((String) uploadResult.get("secure_url")); // Establecer la nueva URL
            paqueteExistente.setImagenPublicId((String) uploadResult.get("public_id")); // Establecer el nuevo public_id
        }
        // <-- Fin lógica Cloudinary -->

        Paquete paqueteActualizado = repo.save(paqueteExistente);
        return paqueteMapper.toDTO(paqueteActualizado);
    }

    // Implementación del método delete
    @Override
    public void delete(Long id) {
        Paquete paqueteAEliminar = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado con id: " + id));

        // <-- Lógica para eliminar la imagen de Cloudinary -->
        String publicId = paqueteAEliminar.getImagenPublicId();
        if (publicId != null && !publicId.isEmpty()) {
            try {
                cloudinaryService.deleteFile(publicId); // Llamar al método de eliminación
            } catch (IOException e) {
                // Manejar error al eliminar de Cloudinary.
                // Decidir si la eliminación del paquete en la BD debe continuar si falla la eliminación en Cloudinary.
                // Aquí optamos por loggear y continuar para no bloquear la eliminación del paquete.
                System.err.println("Error al eliminar la imagen de Cloudinary para el paquete con ID " + id + " y publicId " + publicId + ": " + e.getMessage());
                // Si la eliminación de la imagen es crítica, podrías lanzar una excepción aquí:
                // throw new RuntimeException("No se pudo eliminar la imagen asociada del paquete", e);
            }
        }
        // <-- Fin lógica Cloudinary -->

        // Eliminar el paquete de la base de datos
        repo.delete(paqueteAEliminar);
    }

    @Override
    public Paquete findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado con id: " + id));
    }

    @Override
    public List<Paquete> findAll() {
        return repo.findAll();
    }

    public Optional<Paquete> getByNombre(Paquete.Estado estado) {
        return repo.findByEstado(estado);
    }


    public Page<Paquete> listaPage(Pageable pageable){
        return repo.findAll(pageable);
    }
}

