package org.example.granturismo.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

@Configuration
public class HttpClientConfiguration {

    /**
     * Bean de RestTemplate para realizar llamadas HTTP a APIs externas con timeouts predeterminados.
     * Este es el RestTemplate principal para uso general.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Create a SimpleClientHttpRequestFactory and set timeouts directly on it
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis()); // 10 seconds
        factory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());    // 30 seconds

        // Build the RestTemplate using the configured factory
        return builder.requestFactory(() -> factory).build();
    }

    /**
     * Bean alternativo de RestTemplate específico para servicios de traducción.
     * Tiene timeouts más generosos.
     */
    @Bean("translationRestTemplate")
    public RestTemplate translationRestTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(15).toMillis()); // 15 seconds
        factory.setReadTimeout((int) Duration.ofSeconds(45).toMillis());    // 45 seconds
        return builder.requestFactory(() -> factory).build();
    }

    /**
     * Bean alternativo de RestTemplate específico para servicios de conversión de moneda.
     * Tiene timeouts más estrictos.
     */
    @Bean("currencyRestTemplate")
    public RestTemplate currencyRestTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());  // 5 seconds
        factory.setReadTimeout((int) Duration.ofSeconds(15).toMillis());   // 15 seconds
        return builder.requestFactory(() -> factory).build();
    }
}