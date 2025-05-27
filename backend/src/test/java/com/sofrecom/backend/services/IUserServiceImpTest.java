package com.sofrecom.backend.services;

import com.sofrecom.backend.config.JwtService;
import com.sofrecom.backend.dtos.*;
import com.sofrecom.backend.entities.User;
import com.sofrecom.backend.enums.Role;
import com.sofrecom.backend.exceptions.EmailAlreadyExistsException;
import com.sofrecom.backend.exceptions.ResourceNotFoundException;
import com.sofrecom.backend.repositories.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IUserServiceImpTest {
    @Value("${jwt.secret-key}")
    private String password;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private IUserServiceImp userService;

    private User user;
    private RegisterRequest registerRequest;
    private UserUpdateDto userUpdateDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password(password)
                .role(Role.Administrateur)
                .hasPreference(false)
                .hasSetPassword(false)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setFirstname("John");
        registerRequest.setLastname("Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setRole(Role.Administrateur);

        userUpdateDto = new UserUpdateDto();
        userUpdateDto.setFirstname("Jane");
        userUpdateDto.setLastname("Smith");
        userUpdateDto.setEmail("jane.smith@example.com");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {  //NOSONAR
        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_EmptyList_ShouldReturnEmptyList() {  //NOSONAR
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<User> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void addUser_NewUser_ShouldSaveAndSendEmail() throws MessagingException {  //NOSONAR
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        AuthenticationResponse response = userService.addUser(registerRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.getAccessToken());
        verify(userRepository).save(any(User.class));
        verify(emailService).sendEmail(eq("john.doe@example.com"), eq("Default password"), anyString());
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void addUser_NullRequest_ShouldThrowException() {  //NOSONAR
        assertThrows(NullPointerException.class, () -> userService.addUser(null));
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }



    @Test
    void addUser_EmailExists_ShouldThrowException() throws MessagingException {  //NOSONAR
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () ->
                userService.addUser(registerRequest));
        assertEquals("Email already exists in the database", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void addUser_RepositorySaveFails_ShouldThrowException() {  //NOSONAR
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Save failed"));

        assertThrows(RuntimeException.class, () -> userService.addUser(registerRequest));
        verify(userRepository).save(any(User.class));
    }



    @Test
    void getUsers_WithSearchTerm_ShouldReturnPagedUsers() {  //NOSONAR
        UserDto userDto = new UserDto();
        Page<UserDto> userPage = new PageImpl<>(Arrays.asList(userDto));
        when(userRepository.findBySearchTerm("John", pageable)).thenReturn(userPage);

        Page<UserDto> result = userService.getUsers(pageable, "John");

        assertEquals(1, result.getContent().size());
        verify(userRepository).findBySearchTerm("John", pageable);
    }

    @Test
    void getUsers_WithoutSearchTerm_ShouldReturnAllPagedUsers() {  //NOSONAR
        UserDto userDto = new UserDto();
        Page<UserDto> userPage = new PageImpl<>(Arrays.asList(userDto));
        when(userRepository.findAllUsers(pageable)).thenReturn(userPage);

        Page<UserDto> result = userService.getUsers(pageable, null);

        assertEquals(1, result.getContent().size());
        verify(userRepository).findAllUsers(pageable);
    }

    @Test
    void getUsers_EmptySearchTerm_ShouldReturnAllPagedUsers() {  //NOSONAR
        UserDto userDto = new UserDto();
        Page<UserDto> userPage = new PageImpl<>(Arrays.asList(userDto));
        when(userRepository.findAllUsers(pageable)).thenReturn(userPage);

        Page<UserDto> result = userService.getUsers(pageable, "");

        assertEquals(1, result.getContent().size());
        verify(userRepository).findAllUsers(pageable);
    }



    @Test
    void deleteUser_ShouldDeleteById() {  //NOSONAR
        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_NonExistingId_ShouldNotThrowException() {  //NOSONAR
        doNothing().when(userRepository).deleteById(999L);

        assertDoesNotThrow(() -> userService.deleteUser(999L));
        verify(userRepository).deleteById(999L);
    }

    @Test
    void getUserMinInfo_ShouldReturnMinInfo() {  //NOSONAR
        UserMinDto userMinDto = new UserMinDto();
        when(userRepository.findUserMinInfo("john.doe@example.com")).thenReturn(userMinDto);

        UserMinDto result = userService.getUserMinInfo("john.doe@example.com");

        assertNotNull(result);
        verify(userRepository).findUserMinInfo("john.doe@example.com");
    }

    @Test
    void getUserMinInfo_NullEmail_ShouldReturnNull() {  //NOSONAR
        when(userRepository.findUserMinInfo(null)).thenReturn(null);

        UserMinDto result = userService.getUserMinInfo(null);

        assertNull(result);
        verify(userRepository).findUserMinInfo(null);
    }

    @Test
    void getUserMinInfo_EmptyEmail_ShouldReturnNull() {  //NOSONAR
        when(userRepository.findUserMinInfo("")).thenReturn(null);

        UserMinDto result = userService.getUserMinInfo("");

        assertNull(result);
        verify(userRepository).findUserMinInfo("");
    }

    @Test
    void getUserMinInfo_NonExistingEmail_ShouldReturnNull() { //NOSONAR
        when(userRepository.findUserMinInfo("unknown@example.com")).thenReturn(null);

        UserMinDto result = userService.getUserMinInfo("unknown@example.com");

        assertNull(result);
        verify(userRepository).findUserMinInfo("unknown@example.com");
    }

    @Test
    void updateUser_ExistingUser_ShouldUpdateFields() {  //NOSONAR
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUser(1L, userUpdateDto);

        assertEquals("Jane", result.getFirstname());
        assertEquals("Smith", result.getLastname());
        assertEquals("jane.smith@example.com", result.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_NonExistingUser_ShouldThrowException() {  //NOSONAR
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.updateUser(1L, userUpdateDto));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_NullDto_ShouldThrowException() {  //NOSONAR
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(NullPointerException.class, () -> userService.updateUser(1L, null));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_PartialDto_ShouldUpdateNonNullFields() {  //NOSONAR
        UserUpdateDto partialDto = new UserUpdateDto();
        partialDto.setFirstname("Jane"); // Other fields null
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUser(1L, partialDto);

        assertEquals("Jane", result.getFirstname());
        assertEquals("Doe", result.getLastname()); // Unchanged
        assertEquals("john.doe@example.com", result.getEmail()); // Unchanged
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_RepositorySaveFails_ShouldThrowException() {  //NOSONAR
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Save failed"));

        assertThrows(RuntimeException.class, () -> userService.updateUser(1L, userUpdateDto));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUserById_ExistingUser_ShouldReturnUserDto() {  //NOSONAR
        UserUpdateDto userDto = new UserUpdateDto();
        when(userRepository.findUserUpdateDtoById(1L)).thenReturn(Optional.of(userDto));

        UserUpdateDto result = userService.getUserById(1L);

        assertNotNull(result);
        verify(userRepository).findUserUpdateDtoById(1L);
    }

    @Test
    void getUserById_NonExistingUser_ShouldThrowException() {  //NOSONAR
        when(userRepository.findUserUpdateDtoById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                userService.getUserById(1L));
        assertEquals("User not found with id: 1", exception.getMessage());
    }


    @Test
    void getUserById_NegativeId_ShouldThrowException() {  //NOSONAR
        when(userRepository.findUserUpdateDtoById(-1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                userService.getUserById(-1L));
        assertEquals("User not found with id: -1", exception.getMessage());
        verify(userRepository).findUserUpdateDtoById(-1L);
    }

    @Test
    void findById_ExistingUser_ShouldReturnUser() {  //NOSONAR
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertNotNull(result);
        assertEquals(user, result);
        verify(userRepository).findById(1L);
    }

    @Test
    void findById_NonExistingUser_ShouldReturnNull() {  //NOSONAR
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        User result = userService.findById(1L);

        assertNull(result);
        verify(userRepository).findById(1L);
    }

    @Test
    void findById_NullId_ShouldReturnNull() {  //NOSONAR
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        User result = userService.findById(null);

        assertNull(result);
        verify(userRepository).findById(null);
    }

    @Test
    void findById_NegativeId_ShouldReturnNull() {  //NOSONAR
        when(userRepository.findById(-1L)).thenReturn(Optional.empty());

        User result = userService.findById(-1L);

        assertNull(result);
        verify(userRepository).findById(-1L);
    }

    @Test
    void getIdFromEmail_ShouldReturnId() {  //NOSONAR
        when(userRepository.findIdByEmail("john.doe@example.com")).thenReturn(1L);

        Long result = userService.getIdFromEmail("john.doe@example.com");

        assertEquals(1L, result);
        verify(userRepository).findIdByEmail("john.doe@example.com");
    }

    @Test
    void getIdFromEmail_NullEmail_ShouldReturnNull() {  //NOSONAR
        when(userRepository.findIdByEmail(null)).thenReturn(null);

        Long result = userService.getIdFromEmail(null);

        assertNull(result);
        verify(userRepository).findIdByEmail(null);
    }

    @Test
    void getIdFromEmail_EmptyEmail_ShouldReturnNull() {  //NOSONAR
        when(userRepository.findIdByEmail("")).thenReturn(null);

        Long result = userService.getIdFromEmail("");

        assertNull(result);
        verify(userRepository).findIdByEmail("");
    }

    @Test
    void getIdFromEmail_NonExistingEmail_ShouldReturnNull() {  //NOSONAR
        when(userRepository.findIdByEmail("unknown@example.com")).thenReturn(null);

        Long result = userService.getIdFromEmail("unknown@example.com");

        assertNull(result);
        verify(userRepository).findIdByEmail("unknown@example.com");
    }

    @Test
    void hasSetPassword_ExistingUser_ShouldUpdateAndReturnTrue() {  //NOSONAR
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<Boolean> result = userService.hasSetPassword("john.doe@example.com");

        assertTrue(result.getBody());
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(userRepository).save(user);
    }

    @Test
    void hasSetPassword_NonExistingUser_ShouldReturnFalse() {  //NOSONAR
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        ResponseEntity<Boolean> result = userService.hasSetPassword("john.doe@example.com");

        assertFalse(result.getBody());
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void hasSetPassword_NullEmail_ShouldReturnFalse() { //NOSONAR
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        ResponseEntity<Boolean> result = userService.hasSetPassword(null);

        assertFalse(result.getBody());
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void hasSetPassword_EmptyEmail_ShouldReturnFalse() {  //NOSONAR
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        ResponseEntity<Boolean> result = userService.hasSetPassword("");

        assertFalse(result.getBody());
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void hasSetPassword_RepositorySaveFails_ShouldThrowException() {  //NOSONAR
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Save failed"));

        assertThrows(RuntimeException.class, () -> userService.hasSetPassword("john.doe@example.com"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void numberOfBorrowsByUser_ShouldReturnCount() {  //NOSONAR
        when(userRepository.numbreOfBorrowsByUser("john.doe@example.com")).thenReturn(5L);

        Long result = userService.numberOfBorrowsByUser("john.doe@example.com");

        assertEquals(5L, result);
        verify(userRepository).numbreOfBorrowsByUser("john.doe@example.com");
    }

    @Test
    void numberOfBorrowsByUser_NullEmail_ShouldReturnZero() {  //NOSONAR
        when(userRepository.numbreOfBorrowsByUser(null)).thenReturn(0L);

        Long result = userService.numberOfBorrowsByUser(null);

        assertEquals(0L, result);
        verify(userRepository).numbreOfBorrowsByUser(null);
    }

    @Test
    void numberOfBorrowsByUser_EmptyEmail_ShouldReturnZero() {  //NOSONAR
        when(userRepository.numbreOfBorrowsByUser("")).thenReturn(0L);

        Long result = userService.numberOfBorrowsByUser("");

        assertEquals(0L, result);
        verify(userRepository).numbreOfBorrowsByUser("");
    }

    @Test
    void numberOfBorrowsByUser_NonExistingEmail_ShouldReturnZero() {  //NOSONAR
        when(userRepository.numbreOfBorrowsByUser("unknown@example.com")).thenReturn(0L);

        Long result = userService.numberOfBorrowsByUser("unknown@example.com");

        assertEquals(0L, result);
        verify(userRepository).numbreOfBorrowsByUser("unknown@example.com");
    }

    @Test
    void numberOfBooksByUser_ShouldReturnCount() {  //NOSONAR
        when(userRepository.numbreOfBooksByUser("john.doe@example.com")).thenReturn(3L);

        Long result = userService.numberOfBooksByUser("john.doe@example.com");

        assertEquals(3L, result);
        verify(userRepository).numbreOfBooksByUser("john.doe@example.com");
    }

    @Test
    void numberOfBooksByUser_NullEmail_ShouldReturnZero() {  //NOSONAR
        when(userRepository.numbreOfBooksByUser(null)).thenReturn(0L);

        Long result = userService.numberOfBooksByUser(null);

        assertEquals(0L, result);
        verify(userRepository).numbreOfBooksByUser(null);
    }

    @Test
    void numberOfBooksByUser_EmptyEmail_ShouldReturnZero() {  //NOSONAR
        when(userRepository.numbreOfBooksByUser("")).thenReturn(0L);

        Long result = userService.numberOfBooksByUser("");

        assertEquals(0L, result);
        verify(userRepository).numbreOfBooksByUser("");
    }

    @Test
    void numberOfBooksByUser_NonExistingEmail_ShouldReturnZero() {  //NOSONAR
        when(userRepository.numbreOfBooksByUser("unknown@example.com")).thenReturn(0L);

        Long result = userService.numberOfBooksByUser("unknown@example.com");

        assertEquals(0L, result);
        verify(userRepository).numbreOfBooksByUser("unknown@example.com");
    }


    @Test
    void getTop5Borrowers_EmptyResults_ShouldReturnEmptyList() {  //NOSONAR
        when(userRepository.findTop5Borrowers(Pageable.ofSize(5))).thenReturn(Collections.emptyList());

        List<Top5Dto> result = userService.getTop5Borrowers();

        assertTrue(result.isEmpty());
        verify(userRepository).findTop5Borrowers(Pageable.ofSize(5));
    }




    @Test
    void getTop10Borrowers_EmptyResults_ShouldReturnEmptyList() {  //NOSONAR
        when(userRepository.findTop5Borrowers(Pageable.ofSize(10))).thenReturn(Collections.emptyList());

        List<Top5Dto> result = userService.getTop10Borrowers();

        assertTrue(result.isEmpty());
        verify(userRepository).findTop5Borrowers(Pageable.ofSize(10));
    }


}