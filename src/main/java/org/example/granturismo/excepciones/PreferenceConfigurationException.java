package org.example.granturismo.excepciones;

public class PreferenceConfigurationException extends ExcepcionPersonalizada {
  public PreferenceConfigurationException(String message) {
    super(message);
  }

  public PreferenceConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
