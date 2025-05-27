package com.sofrecom.backend.controllers;

import com.sofrecom.backend.dtos.BookOwerDto;
import com.sofrecom.backend.dtos.BookUpdateDto;
import com.sofrecom.backend.entities.Book;
import com.sofrecom.backend.entities.User;
import com.sofrecom.backend.services.IBookService;
import com.sofrecom.backend.services.IUserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private IBookService bookService;

    @Mock
    private IUserService userService;

    @InjectMocks
    private BookController bookController;

    private Book book;
    private User user;
    private BookUpdateDto bookUpdateDto;
    private BookOwerDto bookOwerDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");
        book.setOwner(user);
        book.setOwnerId(user.getId());

        bookUpdateDto = new BookUpdateDto();
        bookUpdateDto.setTitle("Updated Book");
        bookUpdateDto.setAuthor("Updated Author");
        bookUpdateDto.setIsbn("0987654321");

        bookOwerDto = new BookOwerDto();
        bookOwerDto.setEmail("test@example.com");
    }

    @Test
    void getBookById_Success() {  //NOSONAR
        when(bookService.getBookById(anyLong())).thenReturn(book);

        bookController.getBookById(1L);
        verify(bookService).getBookById(anyLong());
    }

    @Test
    void addSocket_Success() { //NOSONAR
        when(bookService.AddBookSocket(any(Book.class), any(String.class))).thenReturn(book);

        Book result = bookController.updateBook(book, "test@example.com");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Book", result.getTitle());
        assertEquals("Test Author", result.getAuthor());
        assertEquals("1234567890", result.getIsbn());
        verify(bookService).AddBookSocket(any(Book.class), any(String.class));
    }


    @Test
    void getBooksByUser_UserNotFound_ThrowsEntityNotFoundException() {  //NOSONAR
        when(userService.getUserById(anyLong())).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookController.getBooksByUser(999L);
        });

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userService).getUserById(anyLong());
        verifyNoInteractions(bookService);
    }

    @Test
    void addBook_NullOwnerId_ThrowsIllegalArgumentException() {
        Book invalidBook = new Book();
        invalidBook.setTitle("Test Book");
        invalidBook.setAuthor("Test Author");
        invalidBook.setIsbn("1234567890");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookController.addBook(invalidBook);
        });

        assertEquals("L'ownerId est obligatoire !", exception.getMessage());
        verifyNoInteractions(userService, bookService);
    }

    @Test
    void addBook_UserNotFound_ThrowsEntityNotFoundException() {  //NOSONAR
        book.setOwnerId(999L);
        when(userService.findById(anyLong())).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookController.addBook(book);
        });

        assertEquals("Utilisateur non trouvé avec l'ID : 999", exception.getMessage());
        verify(userService).findById(anyLong());
        verifyNoInteractions(bookService);
    }



    @Test
    void addNewBook_Success() {  //NOSONAR
        when(bookService.addNewBook(any(Book.class), any(String.class))).thenReturn(book);

        Book result = bookController.addNewBook(book, "test@example.com");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Book", result.getTitle());
        assertEquals("Test Author", result.getAuthor());
        assertEquals("1234567890", result.getIsbn());
        verify(bookService).addNewBook(any(Book.class), any(String.class));
    }

    @Test
    void getAllBooks_Success() {  //NOSONAR
        when(bookService.getAll()).thenReturn(Collections.singletonList(book));

        List<Book> result = bookController.getAllBooks();

        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
        assertEquals("Test Author", result.get(0).getAuthor());
        assertEquals("1234567890", result.get(0).getIsbn());
        verify(bookService).getAll();
    }

    @Test
    void getAllBooks_EmptyList_Success() {
        when(bookService.getAll()).thenReturn(Collections.emptyList());

        List<Book> result = bookController.getAllBooks();


        assertTrue(result.isEmpty());
        verify(bookService).getAll();
    }


    @Test
    void addBook_Success() {  //NOSONAR
        when(userService.findById(anyLong())).thenReturn(user);
        when(bookService.addBook(any(Book.class))).thenReturn(book);

        ResponseEntity<Book> response = bookController.addBook(book);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test Book", response.getBody().getTitle());
        assertEquals(user, response.getBody().getOwner());
        verify(userService).findById(anyLong());
        verify(bookService).addBook(any(Book.class));
    }




    @Test
    void updateBook_Success() {  //NOSONAR
        Book updatedBook = new Book();
        updatedBook.setId(1L);
        updatedBook.setTitle("Updated Book");
        updatedBook.setAuthor("Updated Author");
        updatedBook.setIsbn("0987654321");
        updatedBook.setOwner(user);
        updatedBook.setOwnerId(user.getId());

        when(bookService.updateBook(anyLong(), any(BookUpdateDto.class))).thenReturn(updatedBook);

        ResponseEntity<Book> response = bookController.updateBook(1L, bookUpdateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Book", response.getBody().getTitle());
        assertEquals("Updated Author", response.getBody().getAuthor());
        assertEquals("0987654321", response.getBody().getIsbn());
        verify(bookService).updateBook(anyLong(), any(BookUpdateDto.class));
    }

    @Test
    void updateBookW_Success() {  //NOSONAR
        Book updatedBook = new Book();
        updatedBook.setId(1L);
        updatedBook.setTitle("Updated Book");
        updatedBook.setAuthor("Updated Author");
        updatedBook.setIsbn("0987654321");
        updatedBook.setOwner(user);
        updatedBook.setOwnerId(user.getId());

        when(bookService.updateBook(anyLong(), any(BookUpdateDto.class))).thenReturn(updatedBook);

        ResponseEntity<Book> response = bookController.updateBookW(1L, bookUpdateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Book", response.getBody().getTitle());
        assertEquals("Updated Author", response.getBody().getAuthor());
        assertEquals("0987654321", response.getBody().getIsbn());
        verify(bookService).updateBook(anyLong(), any(BookUpdateDto.class));
    }

    @Test
    void checkOwnerBookByEmail_Success() {  //NOSONAR
        when(bookService.checkOwnerBookByEmail(any(String.class), anyLong())).thenReturn(true);

        boolean result = bookController.checkOwnerBookByEmail("test@example.com", 1L);

        assertTrue(result);
        verify(bookService).checkOwnerBookByEmail(any(String.class), anyLong());
    }

    @Test
    void getAllBooksWithinEmailOwner_Success() {  //NOSONAR
        when(bookService.getAllBooksWithinEmailOwner(any(String.class))).thenReturn(Collections.singletonList(book));

        List<Book> result = bookController.getAllBooksWithinEmailOwner("test@example.com");

        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
        assertEquals("Test Author", result.get(0).getAuthor());
        assertEquals("1234567890", result.get(0).getIsbn());
        verify(bookService).getAllBooksWithinEmailOwner(any(String.class));
    }

    @Test
    void getAllBooksWithinEmailOwner_EmptyList_Success() {  //NOSONAR
        when(bookService.getAllBooksWithinEmailOwner(any(String.class))).thenReturn(Collections.emptyList());

        List<Book> result = bookController.getAllBooksWithinEmailOwner("test@example.com");

        assertTrue(result.isEmpty());
        verify(bookService).getAllBooksWithinEmailOwner(any(String.class));
    }

    @Test
    void deleteBook_Success() {
        doNothing().when(bookService).deleteBook(anyLong());

        bookController.deleteBook(1L);

        verify(bookService).deleteBook(anyLong());
    }



    @Test
    void getBookOwner_Success() {
        when(bookService.getBookOwnerById(anyLong())).thenReturn(bookOwerDto);

        bookController.getBookOwner(1L);
        verify(bookService).getBookOwnerById(anyLong());
    }



}