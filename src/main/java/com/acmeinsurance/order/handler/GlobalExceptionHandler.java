package com.acmeinsurance.order.handler;

import com.acmeinsurance.order.application.dto.policy.response.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
            final WebExchangeBindException ex, final ServerWebExchange exchange) {

        final String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        final ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed: " + errorMessage)
                .path(exchange.getRequest().getPath().value())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAllUncaughtExceptions(
            final Exception ex, final ServerWebExchange exchange) {

        final ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred: " + ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(PolicyAlreadyCancelledException.class)
    public ResponseEntity<Map<String, Object>> handlePolicyAlreadyCancelledException(final PolicyCancelledException ex) {

        return new ResponseEntity<>(getCancelledBody(ex), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(PolicyCancelledException.class)
    public ResponseEntity<Map<String, Object>> handlePolicyCancelledException(final PolicyCancelledException ex) {

        return new ResponseEntity<>(getCancelledBody(ex), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private static Map<String, Object> getCancelledBody(final PolicyCancelledException ex) {

        final Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put("error", HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
        body.put("message", ex.getMessage());
        body.put("path", "/api/policies/{policyId}");
        return body;
    }

}