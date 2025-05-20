package org.example.granturismo.configuracion;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "ddkdsx3fg",
                "api_key", "296669784924797",
                "api_secret", "mydbfROMEnigdlzje8IrsCtMy3Y"
        ));
    }
}
