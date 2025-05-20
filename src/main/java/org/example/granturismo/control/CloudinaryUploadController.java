package org.example.granturismo.control;

import lombok.RequiredArgsConstructor;
import org.example.granturismo.servicio.ICloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cloudinary") // La ruta base es /cloudinary
public class CloudinaryUploadController {

    private final ICloudinaryService cloudinaryService;

    @PostMapping("/upload") // La ruta completa para este endpoint es /cloudinary/upload
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Cambiar para recibir un Map
            Map uploadResult = cloudinaryService.uploadFile(file);

            // Extraer la secure_url del mapa
            String url = (String) uploadResult.get("secure_url");
            // Opcional: también puedes obtener el public_id si lo necesitas aquí
            // String publicId = (String) uploadResult.get("public_id");

            return ResponseEntity.ok(Map.of("url", url)); // Devolver solo la URL en la respuesta si es lo que esperas el cliente
            // O devolver el mapa completo si el cliente necesita public_id también:
            // return ResponseEntity.ok(uploadResult);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al subir el archivo: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ocurrió un error inesperado: " + e.getMessage()));
        }
    }
}