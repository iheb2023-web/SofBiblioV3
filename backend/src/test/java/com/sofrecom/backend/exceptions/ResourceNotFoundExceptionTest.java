package com.sofrecom.backend.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class ResourceNotFoundExceptionTest {

    @Test
    void testResourceNotFoundExceptionWithMessage() {
        // Arrange
        String message = "Resource not found";

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // Assert
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testResourceNotFoundExceptionWithMessageAndCause() {
        // Arrange
        String message = "User not found";
        Throwable cause = new IllegalArgumentException("Invalid user ID");

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testResourceNotFoundExceptionWithNullValues() {
        // Arrange
        String message = null;
        Throwable cause = null;

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);

        // Assert
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }
}
