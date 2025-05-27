package com.sofrecom.backend.services;

import com.sofrecom.backend.config.JwtService;
import com.sofrecom.backend.dtos.AuthenticationRequest;
import com.sofrecom.backend.dtos.AuthenticationResponse;
import com.sofrecom.backend.entities.User;
import com.sofrecom.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {


    @Value("${jwt.secret-key}")
    private String password;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private AuthenticationRequest authRequest;
    private User user;

    @BeforeEach
    void setUp() {
        authRequest = new AuthenticationRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword(password);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
    }

    @Test
    void authenticate_ValidCredentials_ShouldReturnToken() {
        // Given
        String jwtToken = "mocked-jwt-token";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(jwtToken);

        // When
        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        // Then
        assertNotNull(response);
        assertEquals(jwtToken, response.getAccessToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService).generateToken(user);
    }

    @Test
    void authenticate_UserNotFound_ShouldThrowNoSuchElementException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> authenticationService.authenticate(authRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticate_InvalidCredentials_ShouldThrowBadCredentialsException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class,
                () -> authenticationService.authenticate(authRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticate_DisabledUser_ShouldThrowDisabledException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("User account is disabled"));

        // When & Then
        assertThrows(DisabledException.class,
                () -> authenticationService.authenticate(authRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticate_LockedUser_ShouldThrowLockedException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new LockedException("User account is locked"));

        // When & Then
        assertThrows(LockedException.class,
                () -> authenticationService.authenticate(authRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticate_NullEmail_ShouldThrowException() {
        // Given
        authRequest.setEmail(null);

        // When & Then
        assertThrows(Exception.class, () -> authenticationService.authenticate(authRequest));
    }

    @Test
    void authenticate_EmptyEmail_ShouldThrowException() {
        // Given
        authRequest.setEmail("");

        // When & Then
        assertThrows(Exception.class, () -> authenticationService.authenticate(authRequest));
    }

    @Test
    void authenticate_NullPassword_ShouldThrowException() {
        // Given
        authRequest.setPassword(null);

        // When & Then
        assertThrows(Exception.class, () -> authenticationService.authenticate(authRequest));
    }


    @Test
    void authenticate_NullRequest_ShouldThrowException() {
        // When & Then
        assertThrows(Exception.class, () -> authenticationService.authenticate(null));
    }

    @Test
    void authenticate_ValidCredentials_ShouldUseCorrectAuthenticationToken() {
        // Given
        String jwtToken = "mocked-jwt-token";
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(jwtToken);

        // When
        authenticationService.authenticate(authRequest);

        // Then
        verify(authenticationManager).authenticate(
                argThat(token ->
                        token instanceof UsernamePasswordAuthenticationToken &&
                                "test@example.com".equals(token.getPrincipal())
                )
        );
    }

    @Test
    void authenticate_JwtServiceThrowsException_ShouldPropagateException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenThrow(new RuntimeException("JWT generation failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.authenticate(authRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService).generateToken(user);
    }

    @Test
    void authenticate_DatabaseConnectionError_ShouldPropagateException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("test@example.com"))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.authenticate(authRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticate_DifferentUserEmail_ShouldQueryCorrectEmail() {  //NOSONAR
        // Given
        String differentEmail = "different@example.com";
        authRequest.setEmail(differentEmail);
        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setEmail(differentEmail);

        String jwtToken = "different-jwt-token";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail(differentEmail)).thenReturn(Optional.of(differentUser));
        when(jwtService.generateToken(differentUser)).thenReturn(jwtToken);

        // When
        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        // Then
        assertNotNull(response);
        assertEquals(jwtToken, response.getAccessToken());
        verify(userRepository).findByEmail(differentEmail);
        verify(userRepository, never()).findByEmail("test@example.com");
        verify(jwtService).generateToken(differentUser);
    }

    @Test
    void authenticate_SuccessfulAuthentication_ShouldReturnResponseWithToken() {
        // Given
        String expectedToken = "expected-jwt-token";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(expectedToken);

        // When
        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals(expectedToken, response.getAccessToken());
    }
}