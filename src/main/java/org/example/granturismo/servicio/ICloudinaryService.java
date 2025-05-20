package org.example.granturismo.servicio;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface ICloudinaryService {
    Map uploadFile(MultipartFile file) throws IOException;

    // Método para eliminar un archivo de Cloudinary por su public_id
    void deleteFile(String publicId) throws IOException;
}
