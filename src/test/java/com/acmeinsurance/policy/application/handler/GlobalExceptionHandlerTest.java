package com.acmeinsurance.policy.application.handler;

import com.acmeinsurance.policy.application.dto.response.ErrorResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private ServerWebExchange createMockServerWebExchange(final String pathValue) {
        ServerWebExchange exchangeMock = mock(ServerWebExchange.class, RETURNS_DEEP_STUBS);

        when(exchangeMock.getRequest().getPath().value()).thenReturn(pathValue);

        return exchangeMock;
    }

    @Test
    @DisplayName("Should handle WebExchangeBindException and return 400 Bad Request with validation errors")
    void handleValidationExceptionsShouldReturnBadRequest() {
        final BindingResult bindingResult = mock(BindingResult.class);
        final FieldError fieldError1 = new FieldError("objectName", "field1", "Error message 1");
        final FieldError fieldError2 = new FieldError("objectName", "field2", "Error message 2");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        final MethodParameter methodParameter = mock(MethodParameter.class);
        final WebExchangeBindException ex = new WebExchangeBindException(methodParameter, bindingResult);

        final ServerWebExchange exchange = createMockServerWebExchange("/api/test-path");

        final ResponseEntity<ErrorResponseDTO> responseEntity = handler.handleValidationExceptions(ex, exchange);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        final ErrorResponseDTO errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse.getTimestamp());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), errorResponse.getError());
        assertEquals("Validation failed: field1: Error message 1; field2: Error message 2", errorResponse.getMessage());
        assertEquals("/api/test-path", errorResponse.getPath());
    }

    @Test
    @DisplayName("Should handle generic Exception and return 500 Internal Server Error")
    void handleAllUncaughtExceptionsShouldReturnInternalServerError() {
        final Exception ex = new RuntimeException("Something unexpected happened!");

        final ServerWebExchange exchange = createMockServerWebExchange("/api/generic-error");

        final ResponseEntity<ErrorResponseDTO> responseEntity = handler.handleAllUncaughtExceptions(ex, exchange);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        final ErrorResponseDTO errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse.getTimestamp());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), errorResponse.getError());
        assertEquals("An unexpected error occurred: Something unexpected happened!", errorResponse.getMessage());
        assertEquals("/api/generic-error", errorResponse.getPath());
    }

    @Test
    @DisplayName("Should handle WebExchangeBindException with no field errors")
    void handleValidationExceptionsWithNoFieldErrors() {
        final BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());

        final MethodParameter methodParameter = mock(MethodParameter.class);
        final WebExchangeBindException ex = new WebExchangeBindException(methodParameter, bindingResult);

        final ServerWebExchange exchange = createMockServerWebExchange("/api/no-errors");

        final ResponseEntity<ErrorResponseDTO> responseEntity = handler.handleValidationExceptions(ex, exchange);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Validation failed: ", responseEntity.getBody().getMessage());
    }
}