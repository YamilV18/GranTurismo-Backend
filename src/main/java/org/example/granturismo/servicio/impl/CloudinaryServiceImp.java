package org.example.granturismo.servicio.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.example.granturismo.servicio.ICloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImp implements ICloudinaryService {


    @Autowired
    private Cloudinary cloudinary;

    @Override
    public Map uploadFile(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult;
    }

    @Override
    public void deleteFile(String publicId) throws IOException {
        if (publicId == null || publicId.isEmpty()) {
            return;
        }
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            System.out.println("Cloudinary elimino el resultado para publicId " + publicId + ": " + result);
        } catch (Exception e) {

            System.err.println("Error al eliminar archivo de Cloudinary con publicId " + publicId + ": " + e.getMessage());
        }
    }
}
