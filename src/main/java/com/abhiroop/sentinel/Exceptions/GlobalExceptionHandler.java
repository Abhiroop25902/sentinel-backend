package com.abhiroop.sentinel.Exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorDetails> handleResponseStatusException(ResponseStatusException error, WebRequest request) {
        final var details = ErrorDetails.builder()
                .message(error.getMessage())
                .details(request.getDescription(false))
                .timestamp(Instant.now())
                .build();

        log.error(error.toString(), request.getDescription(false));

        return new ResponseEntity<>(details, error.getStatusCode());

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleException(Exception error, WebRequest request) {
        final var details = ErrorDetails
                .builder()
                .message("An Unexpected error occurred")
                .details(request.getDescription(false))
                .timestamp(Instant.now())
                .build();

        log.error(details.toString(), request.getDescription(false));

        return new ResponseEntity<>(details, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
