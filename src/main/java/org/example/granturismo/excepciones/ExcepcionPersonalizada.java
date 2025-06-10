package org.example.granturismo.excepciones;

public class ExcepcionPersonalizada extends RuntimeException {
    public ExcepcionPersonalizada(String message) {
        super(message);
    }

    public ExcepcionPersonalizada(String message, Throwable cause) {
        super(message, cause);
    }
}