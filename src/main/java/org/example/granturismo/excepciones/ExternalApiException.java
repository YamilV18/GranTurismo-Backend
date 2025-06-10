package org.example.granturismo.excepciones;

public class ExternalApiException extends RuntimeException {
  private final String serviceName;
  private final int statusCode;

  public ExternalApiException(String serviceName, int statusCode, String message) {
    super(String.format("Error en servicio %s (código %d): %s", serviceName, statusCode, message));
    this.serviceName = serviceName;
    this.statusCode = statusCode;
  }

  public ExternalApiException(String serviceName, String message, Throwable cause) {
    super(String.format("Error en servicio %s: %s", serviceName, message), cause);
    this.serviceName = serviceName;
    this.statusCode = 0;
  }

  public String getServiceName() {
    return serviceName;
  }

  public int getStatusCode() {
    return statusCode;
  }
}