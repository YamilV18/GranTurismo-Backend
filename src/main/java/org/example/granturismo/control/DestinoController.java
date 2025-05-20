package org.example.granturismo.control;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.DestinoDTO;
import org.example.granturismo.mappers.DestinoMapper;
import org.example.granturismo.modelo.Destino;
import org.example.granturismo.security.PermitRoles;
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
@RequestMapping("/destinos")
public class DestinoController {

    private final IDestinoService destinoService;
    private final DestinoMapper destinoMapper;

    @GetMapping
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<List<DestinoDTO>> findAll() {
        List<Destino> destinos = destinoService.findAll();
        List<DestinoDTO> list = destinoMapper.toDTOs(destinos);
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<DestinoDTO> findById(@PathVariable("id") Long id) {
        Destino obj = destinoService.findById(id);
        return ResponseEntity.ok(destinoMapper.toDTO(obj));
    }

    @PostMapping
    @PermitRoles({"ADMIN"})
    public ResponseEntity<?> save(
            @Valid @RequestPart("dto") DestinoDTO.DestinoCADTO dto,
            @RequestPart("imagenFile") MultipartFile imagenFile // Ahora se espera el archivo aquí
    ) {
        try {
            // Llama al servicio pasando el DTO y el archivo
            DestinoDTO obj = destinoService.saveD(dto, imagenFile);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getIdDestino()).toUri();
            return ResponseEntity.created(location).body(obj);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error de E/S al procesar la imagen: " + e.getMessage()));
        } catch (IllegalArgumentException e) { // Captura si la imagen obligatoria falta
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ocurrió un error al el Destino: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}") // Espera multipart/form-data
    @PermitRoles({"ADMIN", "PROV"})
    public ResponseEntity<?> update(
            @PathVariable("id") Long id,
            @Valid @RequestPart("dto") DestinoDTO.DestinoCADTO dto,
            @RequestPart(value = "imagenFile", required = false) MultipartFile imagenFile // Archivo de imagen (opcional al actualizar)
    ) {
        try {
            // Llama al servicio pasando el ID, el DTO y el archivo (que puede ser null)
            DestinoDTO obj = destinoService.updateD(dto, id, imagenFile);
            return ResponseEntity.ok(obj);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error de E/S al procesar la imagen al actualizar: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ocurrió un error al actualizar el Detalle: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PermitRoles({"ADMIN"})
    public ResponseEntity<?> delete(@PathVariable("id") Long id) { // Cambiado a ResponseEntity<?> para poder devolver un cuerpo de error si es necesario
        try {
            destinoService.delete(id);
            return ResponseEntity.noContent().build(); // 204 No Content si tiene éxito
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage())); // 404 si no existe
        } catch (Exception e) { // Captura cualquier otro error, incluyendo de Cloudinary si se relanza en el servicio
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al eliminar el Detalle: " + e.getMessage())); // 500
        }
    }

    @GetMapping("/pageable")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<org.springframework.data.domain.Page<DestinoDTO>> listPage(Pageable pageable){
        Page<DestinoDTO> page = destinoService.listaPage(pageable).map(e -> destinoMapper.toDTO(e));
        return ResponseEntity.ok(page);
    }
}
