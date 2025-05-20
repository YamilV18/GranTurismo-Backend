package org.example.granturismo.control;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.ActividadDTO;
import org.example.granturismo.dtos.DestinoDTO;
import org.example.granturismo.mappers.ActividadMapper;
import org.example.granturismo.mappers.DestinoMapper;
import org.example.granturismo.modelo.Actividad;
import org.example.granturismo.modelo.Destino;
import org.example.granturismo.security.PermitRoles;
import org.example.granturismo.servicio.IActividadService;
import org.example.granturismo.servicio.IDestinoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/actividad")
public class ActividadController {


    private final IActividadService actividadService;
    private final ActividadMapper actividadMapper;

    @GetMapping
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<List<ActividadDTO>> findAll() {
        List<Actividad> actividads = actividadService.findAll();
        List<ActividadDTO> list = actividadMapper.toDTOs(actividads);
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<ActividadDTO> findById(@PathVariable("id") Long id) {
        Actividad obj = actividadService.findById(id);
        return ResponseEntity.ok(actividadMapper.toDTO(obj));
    }

    @PostMapping
    @PermitRoles({"ADMIN"})
    public ResponseEntity<?> save(
            @Valid @RequestPart("dto") ActividadDTO.ActividadCADTO dto,
            @RequestPart("imagenFile") MultipartFile imagenFile // Ahora se espera el archivo aquí
    ) {
        try {
            // Llama al servicio pasando el DTO y el archivo
            ActividadDTO obj = actividadService.saveD(dto, imagenFile);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getIdActividad()).toUri();
            return ResponseEntity.created(location).body(obj);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error de E/S al procesar la imagen: " + e.getMessage()));
        } catch (IllegalArgumentException e) { // Captura si la imagen obligatoria falta
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ocurrió un error al crear la actividad: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}") // Espera multipart/form-data
    @PermitRoles({"ADMIN", "PROV"})
    public ResponseEntity<?> update(
            @PathVariable("id") Long id,
            @Valid @RequestPart("dto") ActividadDTO.ActividadCADTO dto,
            @RequestPart(value = "imagenFile", required = false) MultipartFile imagenFile // Archivo de imagen (opcional al actualizar)
    ) {
        try {
            // Llama al servicio pasando el ID, el DTO y el archivo (que puede ser null)
            ActividadDTO obj = actividadService.updateD(dto, id, imagenFile);
            return ResponseEntity.ok(obj);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error de E/S al procesar la imagen al actualizar: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ocurrió un error al actualizar la actividad: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PermitRoles({"ADMIN"})
    public ResponseEntity<?> delete(@PathVariable("id") Long id) { // Cambiado a ResponseEntity<?> para poder devolver un cuerpo de error si es necesario
        try {
            actividadService.delete(id);
            return ResponseEntity.noContent().build(); // 204 No Content si tiene éxito
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage())); // 404 si no existe
        } catch (Exception e) { // Captura cualquier otro error, incluyendo de Cloudinary si se relanza en el servicio
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al eliminar la actividad: " + e.getMessage())); // 500
        }
    }

    @GetMapping("/pageable")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<org.springframework.data.domain.Page<ActividadDTO>> listPage(Pageable pageable){
        Page<ActividadDTO> page = actividadService.listaPage(pageable).map(e -> actividadMapper.toDTO(e));
        return ResponseEntity.ok(page);
    }
}
