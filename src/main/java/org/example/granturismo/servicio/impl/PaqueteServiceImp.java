package org.example.granturismo.servicio.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import net.sf.jasperreports.engine.*; // Only keep if you are actively using JasperReports
import org.example.granturismo.dtos.LocalizedResponseDto;
import org.example.granturismo.dtos.PaqueteDTO;
import org.example.granturismo.mappers.PaqueteMapper;
import org.example.granturismo.modelo.Destino;
import org.example.granturismo.modelo.Paquete;
import org.example.granturismo.modelo.Proveedor;
// import org.example.granturismo.modelo.Rol; // Only keep if you are actively using Rol
import org.example.granturismo.repositorio.ICrudGenericoRepository;
import org.example.granturismo.repositorio.IDestinoRepository;
import org.example.granturismo.repositorio.IPaqueteRepository;
import org.example.granturismo.repositorio.IProveedorRepository;
import org.example.granturismo.servicio.ICloudinaryService;
import org.example.granturismo.servicio.IPaqueteService;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.core.io.ClassPathResource; // Only keep if you are actively using ClassPathResource
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import org.example.granturismo.repositorio.*; // This import is too broad and usually not needed if specific repos are imported
import org.springframework.web.multipart.MultipartFile;


// import javax.sql.DataSource; // Only keep if you are actively using DataSource
import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor

public class PaqueteServiceImp extends CrudGenericoServiceImp<Paquete, Long> implements IPaqueteService {

    @Autowired(required = false) // Mark as false if it's not always injected or if you have a specific config for it
    private DataSource dataSource; // Only keep if you are actively using DataSource

    private final IPaqueteRepository repo;
    private final PaqueteMapper paqueteMapper;
    private final IProveedorRepository proveedorRepository;
    private final IDestinoRepository destinoRepository;
    private final ICloudinaryService cloudinaryService;
    private final LocalizationService localizationService; // Keep this one

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

        // Establecer valores por defecto de localización si no se proporcionan
        if (paquete.getMonedaOriginal() == null || paquete.getMonedaOriginal().isEmpty()) {
            paquete.setMonedaOriginal("PEN");
        }
        if (paquete.getIdiomaOriginal() == null || paquete.getIdiomaOriginal().isEmpty()) {
            paquete.setIdiomaOriginal("es");
        }

        // <-- Lógica para subir la imagen a Cloudinary -->
        if (imagenFile != null && !imagenFile.isEmpty()) {
            Map uploadResult = cloudinaryService.uploadFile(imagenFile); // Subir archivo y obtener resultado
            paquete.setImagenUrl((String) uploadResult.get("secure_url")); // Establecer la URL
            paquete.setImagenPublicId((String) uploadResult.get("public_id")); // Establecer el public_id (NUEVO)
        } else {
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
        paqueteExistente.setPrecioTotal(dto.precioTotal());
        paqueteExistente.setEstado(dto.estado());
        paqueteExistente.setDuracionDias(dto.duracionDias());
        paqueteExistente.setLocalidad(dto.localidad());
        paqueteExistente.setTipoActividad(dto.tipoActividad());
        paqueteExistente.setCuposMaximos(dto.cuposMaximos());
        paqueteExistente.setFechaInicio(dto.fechaInicio());
        paqueteExistente.setFechaFin(dto.fechaFin());

        // Actualizar campos de localización si se proporcionan
        if (dto.monedaOriginal() != null && !dto.monedaOriginal().isEmpty()) {
            paqueteExistente.setMonedaOriginal(dto.monedaOriginal());
        }
        if (dto.idiomaOriginal() != null && !dto.idiomaOriginal().isEmpty()) {
            paqueteExistente.setIdiomaOriginal(dto.idiomaOriginal());
        }

        Proveedor proveedor = proveedorRepository.findById(dto.proveedor())
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));
        Destino destino = destinoRepository.findById(dto.destino())
                .orElseThrow(() -> new EntityNotFoundException("Destino no encontrado"));

        paqueteExistente.setProveedor(proveedor);
        paqueteExistente.setDestino(destino);

        // <-- Lógica para actualizar la imagen con Cloudinary -->
        if (imagenFile != null && !imagenFile.isEmpty()) {
            String oldPublicId = paqueteExistente.getImagenPublicId();
            if (oldPublicId != null && !oldPublicId.isEmpty()) {
                cloudinaryService.deleteFile(oldPublicId);
            }
            Map uploadResult = cloudinaryService.uploadFile(imagenFile);
            paqueteExistente.setImagenUrl((String) uploadResult.get("secure_url"));
            paqueteExistente.setImagenPublicId((String) uploadResult.get("public_id"));
        }

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
                cloudinaryService.deleteFile(publicId);
            } catch (IOException e) {
                System.err.println("Error al eliminar la imagen de Cloudinary para el paquete con ID " + id + " y publicId " + publicId + ": " + e.getMessage());
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


    // NUEVOS MÉTODOS CON LOCALIZACIÓN

    /**
     * Obtiene un paquete por ID, localizado según las preferencias del usuario.
     * Ahora retorna LocalizedResponseDto<PaqueteDTO>.
     */
    @Transactional(readOnly = true)
    public LocalizedResponseDto<PaqueteDTO> getPaqueteLocalizado(Long id, Long userId) {
        log.info("Obteniendo paquete localizado: {} para usuario: {}", id, userId);

        Paquete paquete = findById(id);
        PaqueteDTO paqueteDto = paqueteMapper.toDTO(paquete);

        // Localizar el contenido según las preferencias del usuario.
        // This directly returns the LocalizedResponseDto with the modified PaqueteDTO inside.
        return localizationService.localizeContent(paqueteDto, userId);
    }

    /**
     * Obtiene una lista paginada de paquetes localizados.
     * Ahora retorna Page<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>>.
     */
    @Transactional(readOnly = true)
    public Page<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>> getPaquetesLocalizados(Long userId, Pageable pageable) {
        log.info("Obteniendo lista de paquetes localizados para usuario: {}", userId);

        Page<Paquete> paquetesPage = listaPage(pageable);

        return paquetesPage.map(paquete -> {
            PaqueteDTO.PaqueteListDTO paqueteDto = paqueteMapper.toListDTO(paquete);

            // Localizar cada paquete. This returns a LocalizedResponseDto, which *contains* the modified PaqueteListDTO.
            return localizationService.localizeContent(paqueteDto, userId);
        });
    }

    /**
     * Busca paquetes por estado, localizados.
     * Ahora retorna List<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>>.
     */
    @Transactional(readOnly = true)
    public List<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>> buscarPorEstadoLocalizado(Paquete.Estado estado, Long userId) {
        log.info("Buscando paquetes por estado '{}' para usuario: {}", estado, userId);

        List<Paquete> paquetes = repo.findAll().stream()
                .filter(p -> p.getEstado() == estado)
                .toList();

        return paquetes.stream()
                .map(paquete -> {
                    PaqueteDTO.PaqueteListDTO paqueteDto = paqueteMapper.toListDTO(paquete);

                    return localizationService.localizeContent(paqueteDto, userId);
                })
                .toList();
    }

    /**
     * Método de utilidad para obtener un paquete sin localización (para administradores).
     * This method no longer sets localization metadata on PaqueteDTO directly.
     */
    @Transactional(readOnly = true)
    public PaqueteDTO getPaqueteOriginal(Long id) {
        log.info("Obteniendo paquete original (sin localizar): {}", id);

        Paquete paquete = findById(id);
        PaqueteDTO paqueteDto = paqueteMapper.toDTO(paquete);

        // REMOVE these lines. PaqueteDTO no longer has these fields.
        // paqueteDto.setMonedaAplicada(paquete.getMonedaOriginal());
        // paqueteDto.setIdiomaAplicado(paquete.getIdiomaOriginal());
        // paqueteDto.setFueTraducido(false);
        // paqueteDto.setFueConvertido(false);
        // paqueteDto.setTasaCambio(1.0); // Keep this for the original DTO

        return paqueteDto;
    }

    /**
     * Obtiene estadísticas de localización para un paquete.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEstadisticasLocalizacion(Long id, Long userId) {
        // Now getPaqueteLocalizado returns LocalizedResponseDto, extract the DTO from it.
        LocalizedResponseDto<PaqueteDTO> localizedResponse = getPaqueteLocalizado(id, userId);
        PaqueteDTO paqueteLocalizado = localizedResponse.getData(); // Get the DTO from the response

        PaqueteDTO paqueteOriginal = getPaqueteOriginal(id);

        return Map.of(
                "idiomaOriginal", paqueteOriginal.getIdiomaOriginal(),
                "idiomaAplicado", localizedResponse.getAppliedLanguage(), // Get from LocalizedResponseDto
                "monedaOriginal", paqueteOriginal.getMonedaOriginal(),
                "monedaAplicada", localizedResponse.getAppliedCurrency(), // Get from LocalizedResponseDto
                "precioOriginal", paqueteOriginal.getPrecioTotal(),
                "precioLocalizado", paqueteLocalizado.getPrecioTotal(), // Get from the DTO inside
                "fueTraducido", localizedResponse.isWasTranslated(), // Get from LocalizedResponseDto
                "fueConvertido", localizedResponse.isWasCurrencyConverted(), // Get from LocalizedResponseDto
                "tasaCambio", localizedResponse.getExchangeRate() // Get exchange rate directly from localizedResponse
        );
    }

    /**
     * Extrae la tasa de cambio de la respuesta localizada.
     * This method can now directly use `getExchangeRate()` from `LocalizedResponseDto`.
     */
    private Double extractExchangeRate(LocalizedResponseDto<?> localizedResponse) {
        return localizedResponse.getExchangeRate();
    }
}