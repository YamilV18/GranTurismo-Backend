package org.example.granturismo.servicio.impl;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class EmailTemplateService {

    public String loadTemplate(String templatePath, Map<String, String> variables) {
        try (InputStream inputStream = new ClassPathResource(templatePath).getInputStream()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
            return content;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la plantilla del correo: " + templatePath, e);
        }
    }
}
