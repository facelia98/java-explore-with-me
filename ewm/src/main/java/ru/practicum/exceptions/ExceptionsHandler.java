package ru.practicum.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ExceptionsHandler {

    @ExceptionHandler({ DataIntegrityViolationException.class })
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public Map<String, String> handleValidationDateException(final DataIntegrityViolationException e) {
        return Map.of("409", e.getMessage());
    }

}
