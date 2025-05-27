package com.sofrecom.backend.exceptions;

import com.sofrecom.backend.dtos.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleEmailAlreadyExists() {
        // Arrange
        String exceptionMessage = "Email john@example.com already exists";
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(exceptionMessage);

        // Act
        ResponseEntity<Object> response = globalExceptionHandler.handleEmailAlreadyExists(exception);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ErrorResponse);

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("User already exists with this email.", errorResponse.getMessage());
    }

    @Test
    void testHandleEmailAlreadyExistsWithNullException() {
        // Arrange
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(null);

        // Act
        ResponseEntity<Object> response = globalExceptionHandler.handleEmailAlreadyExists(exception);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ErrorResponse);

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("User already exists with this email.", errorResponse.getMessage());
    }

    @Test
    void testResponseEntityProperties() {
        // Arrange
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("Test message");

        // Act
        ResponseEntity<Object> response = globalExceptionHandler.handleEmailAlreadyExists(exception);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode()); // Instead of getStatusCodeValue()
        assertTrue(response.hasBody());
        assertNotNull(response.getHeaders());
    }

}

