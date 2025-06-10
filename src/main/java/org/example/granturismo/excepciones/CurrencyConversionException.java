package org.example.granturismo.excepciones;

public class CurrencyConversionException extends RuntimeException {
  public CurrencyConversionException(String message) {
    super(message);
  }

  public CurrencyConversionException(String message, Throwable cause) {
    super(message, cause);
  }
}
