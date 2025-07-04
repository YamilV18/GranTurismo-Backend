package org.example.granturismo.excepciones;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse>
    handleAllExceptions(Exception ex, WebRequest request) {
        CustomErrorResponse err = new
                CustomErrorResponse(LocalDateTime.now(), ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(err,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(ModelNotFoundException.class)
    public ResponseEntity<CustomErrorResponse>
    handleModelNotFoundException(ModelNotFoundException ex, WebRequest
            request) {
        CustomErrorResponse err = new
                CustomErrorResponse(LocalDateTime.now(), ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<CustomErrorResponse>
    handleArithmeticException(ArithmeticException ex, WebRequest request) {
        CustomErrorResponse err = new
                CustomErrorResponse(LocalDateTime.now(), ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }
    @Override
    protected ResponseEntity<Object>
    handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                 HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(err ->
                        err.getField().concat(":").concat(err.getDefaultMessage()))
                .collect(Collectors.joining(","));
        CustomErrorResponse err = new
                CustomErrorResponse(LocalDateTime.now(), msg,
                request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }
}
