package com.sofrecom.backend.services;

import com.sofrecom.backend.dtos.BookOwerDto;
import com.sofrecom.backend.dtos.BookUpdateDto;
import com.sofrecom.backend.dtos.UserUpdateDto;
import com.sofrecom.backend.entities.Book;
import com.sofrecom.backend.entities.User;
import com.sofrecom.backend.repositories.BookRepository;
import com.sofrecom.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private IUserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SocketIOService socketIOService;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private User user;
    private BookUpdateDto bookUpdateDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setOwner(user);

        bookUpdateDto = new BookUpdateDto();
        bookUpdateDto.setTitle("Updated Title");
        bookUpdateDto.setAuthor("Updated Author");
    }

    @Test
    void addBook_ShouldSetAddedDateAndSave() { //NoSonar
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.addBook(book);

        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getAddedDate());
        verify(bookRepository).save(book);
    }



    @Test
    void addBook_RepositorySaveFails_ShouldThrowException() { //NoSonar
        when(bookRepository.save(any(Book.class))).thenThrow(new RuntimeException("Save failed"));

        assertThrows(RuntimeException.class, () -> bookService.addBook(book));
        verify(bookRepository).save(book);
    }

    @Test
    void getAll_ShouldReturnAllBooks() { //NoSonar
        List<Book> books = Arrays.asList(book);
        when(bookRepository.findAll()).thenReturn(books);

        List<Book> result = bookService.getAll();

        assertEquals(1, result.size());
        verify(bookRepository).findAll();
    }

    @Test
    void getAll_EmptyList_ShouldReturnEmptyList() { //NoSonar
        when(bookRepository.findAll()).thenReturn(Collections.emptyList());

        List<Book> result = bookService.getAll();

        assertTrue(result.isEmpty());
        verify(bookRepository).findAll();
    }

    @Test
    void getBooksByUser_ValidUserId_ShouldReturnBooks() { //NoSonar
        when(userService.getUserById(1L)).thenReturn(new UserUpdateDto()); // Mocked user DTO
        when(bookRepository.findByOwner_Id(1L)).thenReturn(Arrays.asList(book));

        List<Book> result = bookService.getBooksByUser(1L);

        assertEquals(1, result.size());
        verify(userService).getUserById(1L);
        verify(bookRepository).findByOwner_Id(1L);
    }

    @Test
    void getBooksByUser_InvalidUserId_ShouldThrowException() { //NoSonar
        when(userService.getUserById(1L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                bookService.getBooksByUser(1L));

        assertEquals("User not found with id: 1", exception.getMessage());
        verify(userService).getUserById(1L);
        verify(bookRepository, never()).findByOwner_Id(any());
    }


    @Test
    void getBooksByUser_NoBooks_ShouldReturnEmptyList() { //NoSonar
        when(userService.getUserById(1L)).thenReturn(new UserUpdateDto());
        when(bookRepository.findByOwner_Id(1L)).thenReturn(Collections.emptyList());

        List<Book> result = bookService.getBooksByUser(1L);

        assertTrue(result.isEmpty());
        verify(userService).getUserById(1L);
        verify(bookRepository).findByOwner_Id(1L);
    }

    @Test
    void addNewBook_ShouldSetOwnerAndSave() { //NoSonar
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.addNewBook(book, "test@example.com");

        assertNotNull(result);
        assertEquals(user, result.getOwner());
        assertEquals(LocalDate.now(), result.getAddedDate());
        verify(bookRepository).save(book);
    }



    @Test
    void addNewBook_NonExistentEmail_ShouldSaveWithNullOwner() { //NoSonar
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.addNewBook(book, "unknown@example.com");

        assertNotNull(result);
        assertNull(result.getOwner());
        assertEquals(LocalDate.now(), result.getAddedDate());
        verify(bookRepository).save(book);
    }

    @Test
    void addNewBook_RepositorySaveFails_ShouldThrowException() { //NoSonar
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(bookRepository.save(any(Book.class))).thenThrow(new RuntimeException("Save failed"));

        assertThrows(RuntimeException.class, () ->
                bookService.addNewBook(book, "test@example.com"));
        verify(bookRepository).save(book);
    }

    @Test
    void getBookById_ExistingId_ShouldReturnBook() { //NoSonar
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book result = bookService.getBookById(1L);

        assertNotNull(result);
        assertEquals(book.getId(), result.getId());
        verify(bookRepository).findById(1L);
    }

    @Test
    void getBookById_NonExistingId_ShouldReturnNull() { //NoSonar
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        Book result = bookService.getBookById(1L);

        assertNull(result);
        verify(bookRepository).findById(1L);
    }



    @Test
    void checkOwnerBookByEmail_ShouldCallRepository() { //NoSonar
        when(bookRepository.checkOwnerBookByEmail("test@example.com", 1L)).thenReturn(true);

        boolean result = bookService.checkOwnerBookByEmail("test@example.com", 1L);

        assertTrue(result);
        verify(bookRepository).checkOwnerBookByEmail("test@example.com", 1L);
    }





    @Test
    void checkOwnerBookByEmail_NonExistent_ShouldReturnFalse() { //NoSonar
        when(bookRepository.checkOwnerBookByEmail("unknown@example.com", 999L)).thenReturn(false);

        boolean result = bookService.checkOwnerBookByEmail("unknown@example.com", 999L);

        assertFalse(result);
        verify(bookRepository).checkOwnerBookByEmail("unknown@example.com", 999L);
    }

    @Test
    void getAllBooksWithinEmailOwner_ShouldReturnBooks() { //NoSonar
        when(bookRepository.getAllBooksWithinEmailOwner("test@example.com")).thenReturn(Arrays.asList(book));

        List<Book> result = bookService.getAllBooksWithinEmailOwner("test@example.com");

        assertEquals(1, result.size());
        verify(bookRepository).getAllBooksWithinEmailOwner("test@example.com");
    }



    @Test
    void getAllBooksWithinEmailOwner_NonExistentEmail_ShouldReturnEmptyList() { //NoSonar
        when(bookRepository.getAllBooksWithinEmailOwner("unknown@example.com")).thenReturn(Collections.emptyList());

        List<Book> result = bookService.getAllBooksWithinEmailOwner("unknown@example.com");

        assertTrue(result.isEmpty());
        verify(bookRepository).getAllBooksWithinEmailOwner("unknown@example.com");
    }

    @Test
    void getBookOwnerById_ShouldReturnOwnerDto() { //NoSonar
        BookOwerDto ownerDto = new BookOwerDto();
        when(bookRepository.findBookOwerByIdBook(1L)).thenReturn(ownerDto);

        BookOwerDto result = bookService.getBookOwnerById(1L);

        assertNotNull(result);
        verify(bookRepository).findBookOwerByIdBook(1L);
    }



    @Test
    void getBookOwnerById_NonExistentId_ShouldReturnNull() { //NoSonar
        when(bookRepository.findBookOwerByIdBook(999L)).thenReturn(null);

        BookOwerDto result = bookService.getBookOwnerById(999L);

        assertNull(result);
        verify(bookRepository).findBookOwerByIdBook(999L);
    }

    @Test
    void deleteBook_ShouldCallDeleteById() { //NoSonar
        bookService.deleteBook(1L);

        verify(bookRepository).deleteById(1L);
    }



    @Test
    void deleteBook_NonExistentId_ShouldNotThrowException() { //NoSonar
        doNothing().when(bookRepository).deleteById(999L);

        assertDoesNotThrow(() -> bookService.deleteBook(999L));
        verify(bookRepository).deleteById(999L);
    }

    @Test
    void updateBook_ExistingBook_ShouldUpdateFields() { //NoSonar
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.updateBook(1L, bookUpdateDto);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Author", result.getAuthor());
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_NonExistingBook_ShouldThrowException() { //NoSonar
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                bookService.updateBook(1L, bookUpdateDto));

        assertEquals("Book not found", exception.getMessage());
        verify(bookRepository, never()).save(any());
    }




    @Test
    void updateBook_EmptyDto_ShouldNotUpdateFields() { //NoSonar
        BookUpdateDto emptyDto = new BookUpdateDto();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.updateBook(1L, emptyDto);

        assertNotNull(result);
        assertEquals("Test Book", result.getTitle()); // Unchanged
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_RepositorySaveFails_ShouldThrowException() { //NoSonar
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenThrow(new RuntimeException("Save failed"));

        assertThrows(RuntimeException.class, () ->
                bookService.updateBook(1L, bookUpdateDto));
        verify(bookRepository).save(book);
    }

    @Test
    void addBookSocket_ShouldSetOwnerAndSave() { //NoSonar
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.AddBookSocket(book, "test@example.com");

        assertNotNull(result);
        assertEquals(user, result.getOwner());
        assertEquals(LocalDate.now(), result.getAddedDate());
        verify(bookRepository).save(book);
    }


    @Test
    void addBookSocket_NonExistentEmail_ShouldSaveWithNullOwner() { //NoSonar
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.AddBookSocket(book, "unknown@example.com");

        assertNotNull(result);
        assertNull(result.getOwner());
        assertEquals(LocalDate.now(), result.getAddedDate());
        verify(bookRepository).save(book);
    }

    @Test
    void addBookSocket_RepositorySaveFails_ShouldThrowException() { //NoSonar
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(bookRepository.save(any(Book.class))).thenThrow(new RuntimeException("Save failed"));

        assertThrows(RuntimeException.class, () ->
                bookService.AddBookSocket(book, "test@example.com"));
        verify(bookRepository).save(book);
    }

    // Additional test methods to add to your existing BookServiceTest class

    @Test
    void updateBook_UpdateAllFields_ShouldUpdateAllBookFields() { //NoSonar
        // Arrange
        BookUpdateDto fullUpdateDto = new BookUpdateDto();
        fullUpdateDto.setTitle("New Title");
        fullUpdateDto.setAuthor("New Author");
        fullUpdateDto.setDescription("New Description");
        fullUpdateDto.setCoverUrl("http://example.com/cover.jpg");
        fullUpdateDto.setPublishedDate("2023-01-01"); // String instead of LocalDate
        fullUpdateDto.setIsbn("978-1234567890");
        fullUpdateDto.setCategory("Fiction");
        fullUpdateDto.setPageCount(300);
        fullUpdateDto.setLanguage("English");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.updateBook(1L, fullUpdateDto);

        // Assert
        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals("New Author", result.getAuthor());
        assertEquals("New Description", result.getDescription());
        assertEquals("http://example.com/cover.jpg", result.getCoverUrl());
        assertEquals("2023-01-01", result.getPublishedDate()); // String comparison
        assertEquals("978-1234567890", result.getIsbn());
        assertEquals("Fiction", result.getCategory());
        assertEquals(300, result.getPageCount());
        assertEquals("English", result.getLanguage());
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_UpdateOnlyTitle_ShouldUpdateOnlyTitle() { //NoSonar
        // Arrange
        BookUpdateDto titleOnlyDto = new BookUpdateDto();
        titleOnlyDto.setTitle("Only Title Updated");

        // Set initial values
        book.setAuthor("Original Author");
        book.setDescription("Original Description");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.updateBook(1L, titleOnlyDto);

        // Assert
        assertNotNull(result);
        assertEquals("Only Title Updated", result.getTitle());
        assertEquals("Original Author", result.getAuthor()); // Should remain unchanged
        assertEquals("Original Description", result.getDescription()); // Should remain unchanged
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_UpdateOnlyAuthor_ShouldUpdateOnlyAuthor() { //NoSonar
        // Arrange
        BookUpdateDto authorOnlyDto = new BookUpdateDto();
        authorOnlyDto.setAuthor("New Author Only");

        // Set initial values
        book.setTitle("Original Title");
        book.setDescription("Original Description");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.updateBook(1L, authorOnlyDto);

        // Assert
        assertNotNull(result);
        assertEquals("Original Title", result.getTitle()); // Should remain unchanged
        assertEquals("New Author Only", result.getAuthor());
        assertEquals("Original Description", result.getDescription()); // Should remain unchanged
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_UpdateDescription_ShouldUpdateDescription() { //NoSonar
        // Arrange
        BookUpdateDto descriptionDto = new BookUpdateDto();
        descriptionDto.setDescription("Updated book description with more details");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.updateBook(1L, descriptionDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated book description with more details", result.getDescription());
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_UpdateCoverUrl_ShouldUpdateCoverUrl() { //NoSonar
        // Arrange
        BookUpdateDto coverUrlDto = new BookUpdateDto();
        coverUrlDto.setCoverUrl("https://newcover.example.com/book-cover.png");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.updateBook(1L, coverUrlDto);

        // Assert
        assertNotNull(result);
        assertEquals("https://newcover.example.com/book-cover.png", result.getCoverUrl());
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_UpdatePublishedDate_ShouldUpdatePublishedDate() { //NoSonar
        // Arrange
        BookUpdateDto publishedDateDto = new BookUpdateDto();
        String newPublishedDate = "2022-05-15"; // String instead of LocalDate
        publishedDateDto.setPublishedDate(newPublishedDate);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.updateBook(1L, publishedDateDto);

        // Assert
        assertNotNull(result);
        assertEquals(newPublishedDate, result.getPublishedDate()); // String comparison
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_UpdateIsbn_ShouldUpdateIsbn() { //NoSonar
        // Arrange
        BookUpdateDto isbnDto = new BookUpdateDto();
        isbnDto.setIsbn("978-0123456789");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.updateBook(1L, isbnDto);

        // Assert
        assertNotNull(result);
        assertEquals("978-0123456789", result.getIsbn());
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_UpdateCategory_ShouldUpdateCategory() { //NoSonar
        // Arrange
        BookUpdateDto categoryDto = new BookUpdateDto();
        categoryDto.setCategory("Science Fiction");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.updateBook(1L, categoryDto);

        // Assert
        assertNotNull(result);
        assertEquals("Science Fiction", result.getCategory());
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_UpdatePageCount_ShouldUpdatePageCount() { //NoSonar
        // Arrange
        BookUpdateDto pageCountDto = new BookUpdateDto();
        pageCountDto.setPageCount(450);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.updateBook(1L, pageCountDto);

        // Assert
        assertNotNull(result);
        assertEquals(450, result.getPageCount());
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_PageCountZero_ShouldNotUpdatePageCount() { //NoSonar
        // Arrange
        BookUpdateDto pageCountDto = new BookUpdateDto();
        pageCountDto.setPageCount(0); // This should not update the page count

        book.setPageCount(200); // Set original page count

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.updateBook(1L, pageCountDto);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getPageCount()); // Should remain unchanged
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_UpdateLanguage_ShouldUpdateLanguage() { //NoSonar
        // Arrange
        BookUpdateDto languageDto = new BookUpdateDto();
        languageDto.setLanguage("French");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.updateBook(1L, languageDto);

        // Assert
        assertNotNull(result);
        assertEquals("French", result.getLanguage());
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_PartialUpdate_ShouldUpdateOnlyProvidedFields() { //NoSonar
        // Arrange
        BookUpdateDto partialDto = new BookUpdateDto();
        partialDto.setTitle("Partially Updated Title");
        partialDto.setPageCount(250);
        partialDto.setLanguage("Spanish");
        // Other fields are null and should not be updated

        // Set initial values
        book.setAuthor("Original Author");
        book.setDescription("Original Description");
        book.setIsbn("Original ISBN");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.updateBook(1L, partialDto);

        // Assert
        assertNotNull(result);
        assertEquals("Partially Updated Title", result.getTitle()); // Updated
        assertEquals(250, result.getPageCount()); // Updated
        assertEquals("Spanish", result.getLanguage()); // Updated
        assertEquals("Original Author", result.getAuthor()); // Should remain unchanged
        assertEquals("Original Description", result.getDescription()); // Should remain unchanged
        assertEquals("Original ISBN", result.getIsbn()); // Should remain unchanged
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_NullStringFields_ShouldNotUpdateNullFields() { //NoSonar
        // Arrange
        BookUpdateDto nullFieldsDto = new BookUpdateDto();
        nullFieldsDto.setTitle("New Title");
        // All other fields are null
        nullFieldsDto.setAuthor(null);
        nullFieldsDto.setDescription(null);
        nullFieldsDto.setCoverUrl(null);
        nullFieldsDto.setIsbn(null);
        nullFieldsDto.setCategory(null);
        nullFieldsDto.setLanguage(null);
        nullFieldsDto.setPublishedDate(null);

        // Set initial values
        book.setAuthor("Should Not Change");
        book.setDescription("Should Not Change");
        book.setIsbn("Should Not Change");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.updateBook(1L, nullFieldsDto);

        // Assert
        assertNotNull(result);
        assertEquals("New Title", result.getTitle()); // Only this should be updated
        assertEquals("Should Not Change", result.getAuthor());
        assertEquals("Should Not Change", result.getDescription());
        assertEquals("Should Not Change", result.getIsbn());
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_EmptyStringFields_ShouldUpdateWithEmptyStrings() { //NoSonar
        // Arrange
        BookUpdateDto emptyStringDto = new BookUpdateDto();
        emptyStringDto.setTitle(""); // Empty string, not null
        emptyStringDto.setAuthor(""); // Empty string, not null
        emptyStringDto.setDescription("Valid Description"); // Non-empty for comparison

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.updateBook(1L, emptyStringDto);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getTitle()); // Should be updated to empty string
        assertEquals("", result.getAuthor()); // Should be updated to empty string
        assertEquals("Valid Description", result.getDescription()); // Should be updated
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_ValidIdButRepositoryThrowsException_ShouldPropagateException() { //NoSonar
        // Arrange
        BookUpdateDto updateDto = new BookUpdateDto();
        updateDto.setTitle("New Title");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                bookService.updateBook(1L, updateDto));

        assertEquals("Database connection failed", exception.getMessage());
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(book);
    }
}