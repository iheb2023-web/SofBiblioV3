package com.sofrecom.backend.controllers;

import com.sofrecom.backend.dtos.*;
import com.sofrecom.backend.entities.User;
import com.sofrecom.backend.enums.Role;
import com.sofrecom.backend.services.AuthenticationService;
import com.sofrecom.backend.services.CloudinaryService;
import com.sofrecom.backend.services.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private IUserService userService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private UserController userController;


    @Value("${jwt.secret-key}")
    private String password;
    private User user;
    private UserDto userDto;
    private UserUpdateDto userUpdateDto;
    private AuthenticationResponse authResponse;
    private RegisterRequest registerRequest;
    private AuthenticationRequest authRequest;
    private UserMinDto userMinDto;
    private Top5Dto top5Dto;
    private MultipartFile file;

    @BeforeEach
    void setUp() {
        user = createTestUser();
        userDto = createTestUserDto();
        userUpdateDto = createTestUserUpdateDto();
        authResponse = createTestAuthResponse();
        registerRequest = createTestRegisterRequest();
        authRequest = createTestAuthRequest();
        userMinDto = createTestUserMinDto();
        top5Dto = createTestTop5Dto();
        file = mock(MultipartFile.class);
    }

    // Helper methods for test data setup
    private User createTestUser() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        return testUser;
    }


    private UserDto createTestUserDto() {
        return new UserDto(1L, "John", "Doe", "test@example.com", "http://image.url", "Engineer", Role.Collaborateur);
    }

    private UserUpdateDto createTestUserUpdateDto() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("test@example.com");
        dto.setFirstname("John");
        dto.setLastname("Doe");
        dto.setJob("Engineer");
        return dto;
    }

    private AuthenticationResponse createTestAuthResponse() {
        return AuthenticationResponse.builder()
                .accessToken("jwt-token")
                .build();
    }

    private RegisterRequest createTestRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        return request;
    }

    private AuthenticationRequest createTestAuthRequest() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("test@example.com");
        request.setPassword(password);
        return request;
    }

    private UserMinDto createTestUserMinDto() {
        UserMinDto dto = new UserMinDto();
        dto.setEmail("test@example.com");
        return dto;
    }

    private Top5Dto createTestTop5Dto() {
        return new Top5Dto(1L, "John", "Doe", "test@example.com", 10L);
    }

    @Test
    void registerUser_Success() {
        when(userService.addUser(any(RegisterRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthenticationResponse> response = userController.registerUser(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("jwt-token", response.getBody().getAccessToken());
        verify(userService).addUser(any(RegisterRequest.class));
    }

    @Test
    void authenticate_Success() {
        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthenticationResponse> response = userController.authenticate(authRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", response.getBody().getAccessToken());
        verify(authenticationService).authenticate(any(AuthenticationRequest.class));
    }

    @Test
    void getUsers_WithSearch_Success() {
        Page<UserDto> userPage = new PageImpl<>(Collections.singletonList(userDto));
        when(userService.getUsers(any(Pageable.class), anyString())).thenReturn(userPage);

        Page<UserDto> result = userController.getUsers(0, 5, "test");

        assertEquals(1, result.getContent().size());
        assertEquals("test@example.com", result.getContent().get(0).getEmail());
        assertEquals("John", result.getContent().get(0).getFirstname());
        assertEquals("Doe", result.getContent().get(0).getLastname());
        assertEquals("Engineer", result.getContent().get(0).getJob());
        assertEquals("http://image.url", result.getContent().get(0).getImage());
        assertEquals(Role.Collaborateur, result.getContent().get(0).getRole());
        verify(userService).getUsers(any(Pageable.class), anyString());
    }

    @Test
    void getUsers_WithoutSearch_Success() {
        Page<UserDto> userPage = new PageImpl<>(Collections.singletonList(userDto));
        when(userService.getUsers(any(Pageable.class), isNull())).thenReturn(userPage);

        Page<UserDto> result = userController.getUsers(0, 5, null);

        assertEquals(1, result.getContent().size());
        assertEquals("test@example.com", result.getContent().get(0).getEmail());
        verify(userService).getUsers(any(Pageable.class), isNull());
    }

    @Test
    void getUserMinInfo_Success() {
        when(userService.getUserMinInfo(anyString())).thenReturn(userMinDto);

        UserMinDto result = userController.getUserMinInfo("test@example.com");

        assertEquals("test@example.com", result.getEmail());
        verify(userService).getUserMinInfo(anyString());
    }

    @Test
    void uploadImage_Success() throws Exception {
        when(cloudinaryService.uploadImage(any(MultipartFile.class))).thenReturn("http://image.url");

        String result = userController.uploadImage(file);

        assertEquals("http://image.url", result);
        verify(cloudinaryService).uploadImage(any(MultipartFile.class));
    }

    @Test
    void updateUser_Success() {
        when(userService.updateUser(eq(1L), any(UserUpdateDto.class))).thenReturn(user);

        ResponseEntity<User> response = userController.updateUser(1L, userUpdateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test@example.com", response.getBody().getEmail());
        verify(userService).updateUser(eq(1L), any(UserUpdateDto.class));
    }

    @Test
    void getUserDetail_Success() {
        when(userService.getUserById(1L)).thenReturn(userUpdateDto);

        ResponseEntity<UserUpdateDto> response = userController.getuserdetail(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals("John", response.getBody().getFirstname());
        assertEquals("Doe", response.getBody().getLastname());
        assertEquals("Engineer", response.getBody().getJob());
        verify(userService).getUserById(1L);
    }

    @Test
    void deleteUser_Success() {
        doNothing().when(userService).deleteUser(1L);

        userController.deleteUser(1L);

        verify(userService).deleteUser(1L);
    }

    @Test
    void getIdFromEmail_Success() {
        when(userService.getIdFromEmail(anyString())).thenReturn(1L);

        Long result = userController.getIdFromEmail("test@example.com");

        assertEquals(1L, result);
        verify(userService).getIdFromEmail(anyString());
    }

    @Test
    void numberOfBorrowsByUser_Success() {
        when(userService.numberOfBorrowsByUser(anyString())).thenReturn(5L);

        Long result = userController.numberOfBorrowsByUser("test@example.com");

        assertEquals(5L, result);
        verify(userService).numberOfBorrowsByUser(anyString());
    }

    @Test
    void numberOfBooksByUser_Success() {
        when(userService.numberOfBooksByUser(anyString())).thenReturn(3L);

        Long result = userController.numberOfBooksByUser("test@example.com");

        assertEquals(3L, result);
        verify(userService).numberOfBooksByUser(anyString());
    }

    @Test
    void hasSetPassword_Success() {
        when(userService.hasSetPassword(anyString())).thenReturn(ResponseEntity.ok(true));

        ResponseEntity<Boolean> response = userController.hasSetPassword("test@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(userService).hasSetPassword(anyString());
    }

    @Test
    void getTop5Borrowers_Success() {
        List<Top5Dto> top5List = Collections.singletonList(top5Dto);
        when(userService.getTop5Borrowers()).thenReturn(top5List);

        ResponseEntity<List<Top5Dto>> response = userController.getTop5Borrowers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("test@example.com", response.getBody().get(0).getEmail());
        assertEquals("John", response.getBody().get(0).getFirstname());
        assertEquals("Doe", response.getBody().get(0).getLastname());
        assertEquals(10L, response.getBody().get(0).getNbEmprunts());
        verify(userService).getTop5Borrowers();
    }

    @Test
    void getTop5Borrowers_EmptyList_ReturnsEmpty() {
        when(userService.getTop5Borrowers()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Top5Dto>> response = userController.getTop5Borrowers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(userService).getTop5Borrowers();
    }

    @Test
    void getTop10Borrowers_Success() {
        List<Top5Dto> top10List = createTestTop10List();
        when(userService.getTop10Borrowers()).thenReturn(top10List);

        ResponseEntity<List<Top5Dto>> response = userController.getTop10Borrowers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10, response.getBody().size());
        assertEquals("john@example.com", response.getBody().get(0).getEmail());
        assertEquals(25L, response.getBody().get(0).getNbEmprunts());
        assertEquals("maria@example.com", response.getBody().get(9).getEmail());
        assertEquals(2L, response.getBody().get(9).getNbEmprunts());
        verify(userService).getTop10Borrowers();
    }

    @Test
    void getTop10Borrowers_EmptyList_ReturnsEmpty() {
        when(userService.getTop10Borrowers()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Top5Dto>> response = userController.getTop10Borrowers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(userService).getTop10Borrowers();
    }

    @Test
    void getTop10Borrowers_LessThan10Users_ReturnsAvailableUsers() {
        List<Top5Dto> partialList = List.of(
                new Top5Dto(1L, "John", "Doe", "john@example.com", 15L),
                new Top5Dto(2L, "Jane", "Smith", "jane@example.com", 10L),
                new Top5Dto(3L, "Mike", "Johnson", "mike@example.com", 5L)
        );
        when(userService.getTop10Borrowers()).thenReturn(partialList);

        ResponseEntity<List<Top5Dto>> response = userController.getTop10Borrowers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertEquals("john@example.com", response.getBody().get(0).getEmail());
        assertEquals(15L, response.getBody().get(0).getNbEmprunts());
        verify(userService).getTop10Borrowers();
    }

    @Test
    void getTop10Borrowers_SingleUser_ReturnsSingleUser() {
        List<Top5Dto> singleUserList = Collections.singletonList(
                new Top5Dto(1L, "John", "Doe", "john@example.com", 5L)
        );
        when(userService.getTop10Borrowers()).thenReturn(singleUserList);

        ResponseEntity<List<Top5Dto>> response = userController.getTop10Borrowers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("john@example.com", response.getBody().get(0).getEmail());
        assertEquals(5L, response.getBody().get(0).getNbEmprunts());
        verify(userService).getTop10Borrowers();
    }

    @Test
    void getTop10Borrowers_ServiceThrowsException_PropagatesException() {
        when(userService.getTop10Borrowers()).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> userController.getTop10Borrowers());
        verify(userService).getTop10Borrowers();
    }

    @Test
    void getTop10Borrowers_VerifyOrderIsPreserved() {
        List<Top5Dto> orderedList = List.of(
                new Top5Dto(1L, "First", "User", "first@example.com", 100L),
                new Top5Dto(2L, "Second", "User", "second@example.com", 50L),
                new Top5Dto(3L, "Third", "User", "third@example.com", 25L)
        );
        when(userService.getTop10Borrowers()).thenReturn(orderedList);

        ResponseEntity<List<Top5Dto>> response = userController.getTop10Borrowers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Top5Dto> result = response.getBody();
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("first@example.com", result.get(0).getEmail());
        assertEquals(100L, result.get(0).getNbEmprunts());
        assertEquals("second@example.com", result.get(1).getEmail());
        assertEquals(50L, result.get(1).getNbEmprunts());
        assertEquals("third@example.com", result.get(2).getEmail());
        assertEquals(25L, result.get(2).getNbEmprunts());
        verify(userService).getTop10Borrowers();
    }

    // New negative test case
    @Test
    void getUserMinInfo_InvalidEmail_ThrowsException() {
        when(userService.getUserMinInfo(anyString())).thenThrow(new IllegalArgumentException("User not found"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userController.getUserMinInfo("invalid@example.com"));
        assertEquals("User not found", exception.getMessage());
        verify(userService).getUserMinInfo(anyString());
    }

    // Helper method for top 10 borrowers test data
    private List<Top5Dto> createTestTop10List() {
        return List.of(
                new Top5Dto(1L, "John", "Doe", "john@example.com", 25L),
                new Top5Dto(2L, "Jane", "Smith", "jane@example.com", 20L),
                new Top5Dto(3L, "Mike", "Johnson", "mike@example.com", 18L),
                new Top5Dto(4L, "Sarah", "Wilson", "sarah@example.com", 15L),
                new Top5Dto(5L, "David", "Brown", "david@example.com", 12L),
                new Top5Dto(6L, "Emma", "Davis", "emma@example.com", 10L),
                new Top5Dto(7L, "James", "Miller", "james@example.com", 8L),
                new Top5Dto(8L, "Lisa", "Garcia", "lisa@example.com", 6L),
                new Top5Dto(9L, "Robert", "Martinez", "robert@example.com", 4L),
                new Top5Dto(10L, "Maria", "Lopez", "maria@example.com", 2L)
        );
    }
}