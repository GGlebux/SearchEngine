package engine.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static java.lang.System.err;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public void handleDataIntegrityViolation(DataIntegrityViolationException e) {
        if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
            err.println("Duplicate entry detected and ignored: " + e.getMessage());
        }
    }
}
