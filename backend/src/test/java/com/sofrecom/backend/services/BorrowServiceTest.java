package com.sofrecom.backend.services;

import com.sofrecom.backend.dtos.BorrowStatusUser;
import com.sofrecom.backend.dtos.OccupiedDates;
import com.sofrecom.backend.entities.Book;
import com.sofrecom.backend.entities.Borrow;
import com.sofrecom.backend.entities.User;
import com.sofrecom.backend.enums.BorrowStatus;
import com.sofrecom.backend.repositories.BorrowRepository;
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
class BorrowServiceTest {

    @Mock
    private BorrowRepository borrowRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SocketIOService socketIOService;

    @InjectMocks
    private BorrowService borrowService;

    private Borrow borrow;
    private User borrower;
    private Book book;

    @BeforeEach
    void setUp() {
        borrower = new User();
        borrower.setId(1L);
        borrower.setEmail("borrower@example.com");

        book = new Book();
        book.setId(1L);

        borrow = new Borrow();
        borrow.setId(1L);
        borrow.setBorrower(borrower);
        borrow.setBook(book);
        borrow.setBorrowStatus(BorrowStatus.PENDING);
    }

    @Test
    void findAll_ShouldReturnAllBorrows() { //NoSonar
        List<Borrow> borrows = Arrays.asList(borrow);
        when(borrowRepository.findAll()).thenReturn(borrows);

        List<Borrow> result = borrowService.findAll();

        assertEquals(1, result.size());
        verify(borrowRepository).findAll();
    }

    @Test
    void findAll_EmptyList_ShouldReturnEmptyList() { //NoSonar
        when(borrowRepository.findAll()).thenReturn(Collections.emptyList());

        List<Borrow> result = borrowService.findAll();

        assertTrue(result.isEmpty());
        verify(borrowRepository).findAll();
    }

    @Test
    void borrowBook_ShouldSetRequestDateAndSave() { //NoSonar
        when(userRepository.findByEmail("borrower@example.com")).thenReturn(Optional.of(borrower));
        when(borrowRepository.save(any(Borrow.class))).thenReturn(borrow);

        borrowService.borrowBook(borrow);

        assertEquals(LocalDate.now(), borrow.getRequestDate());
        assertEquals(BorrowStatus.PENDING, borrow.getBorrowStatus());
        assertEquals(borrower.getId(), borrow.getBorrower().getId());
        verify(borrowRepository).save(borrow);
        verify(socketIOService).sendDemandNotification(borrow.getId());
    }

    @Test
    void borrowBook_NullBorrow_ShouldThrowException() { //NoSonar
        assertThrows(NullPointerException.class, () -> borrowService.borrowBook(null));
        verify(userRepository, never()).findByEmail(any());
        verify(borrowRepository, never()).save(any());
        verify(socketIOService, never()).sendDemandNotification(any());
    }

    @Test
    void borrowBook_NonExistentUser_ShouldSaveWithoutIdUpdate() { //NoSonar
        when(userRepository.findByEmail("borrower@example.com")).thenReturn(Optional.empty());
        when(borrowRepository.save(any(Borrow.class))).thenReturn(borrow);

        borrowService.borrowBook(borrow);

        assertEquals(LocalDate.now(), borrow.getRequestDate());
        assertEquals(BorrowStatus.PENDING, borrow.getBorrowStatus());
        assertEquals(borrower.getId(), borrow.getBorrower().getId()); // ID unchanged
        verify(borrowRepository).save(borrow);
        verify(socketIOService).sendDemandNotification(borrow.getId());
    }

    @Test
    void borrowBook_RepositorySaveFails_ShouldThrowException() { //NoSonar
        when(userRepository.findByEmail("borrower@example.com")).thenReturn(Optional.of(borrower));
        when(borrowRepository.save(any(Borrow.class))).thenThrow(new RuntimeException("Save failed"));

        assertThrows(RuntimeException.class, () -> borrowService.borrowBook(borrow));
        verify(borrowRepository).save(borrow);
        verify(socketIOService, never()).sendDemandNotification(any());
    }

    @Test
    void processBorrowRequest_Approved_ShouldUpdateStatus() { //NoSonar
        when(borrowRepository.save(any(Borrow.class))).thenReturn(borrow);

        Borrow result = borrowService.processBorrowRequest(borrow, true);

        assertEquals(LocalDate.now(), result.getResponseDate());
        assertEquals(BorrowStatus.APPROVED, result.getBorrowStatus());
        verify(borrowRepository).save(borrow);
        verify(socketIOService).sendProcessBorrowRequestNotification(borrow.getId());
    }

    @Test
    void processBorrowRequest_Rejected_ShouldUpdateStatus() { //NoSonar
        when(borrowRepository.save(any(Borrow.class))).thenReturn(borrow);

        Borrow result = borrowService.processBorrowRequest(borrow, false);

        assertEquals(LocalDate.now(), result.getResponseDate());
        assertEquals(BorrowStatus.REJECTED, result.getBorrowStatus());
        verify(borrowRepository).save(borrow);
        verify(socketIOService).sendProcessBorrowRequestNotification(borrow.getId());
    }

    @Test
    void processBorrowRequest_NullBorrow_ShouldThrowException() { //NoSonar
        assertThrows(NullPointerException.class, () -> borrowService.processBorrowRequest(null, true));
        verify(borrowRepository, never()).save(any());
        verify(socketIOService, never()).sendProcessBorrowRequestNotification(any());
    }

    @Test
    void processBorrowRequest_RepositorySaveFails_ShouldThrowException() { //NoSonar
        when(borrowRepository.save(any(Borrow.class))).thenThrow(new RuntimeException("Save failed"));

        assertThrows(RuntimeException.class, () -> borrowService.processBorrowRequest(borrow, true));
        verify(borrowRepository).save(borrow);
    }

    @Test
    void getBorrowById_ExistingId_ShouldReturnBorrow() { //NoSonar
        when(borrowRepository.findById(1L)).thenReturn(Optional.of(borrow));

        Borrow result = borrowService.getBorrowById(1L);

        assertNotNull(result);
        assertEquals(borrow.getId(), result.getId());
        verify(borrowRepository).findById(1L);
    }

    @Test
    void getBorrowById_NonExistingId_ShouldReturnNull() { //NoSonar
        when(borrowRepository.findById(1L)).thenReturn(Optional.empty());

        Borrow result = borrowService.getBorrowById(1L);

        assertNull(result);
        verify(borrowRepository).findById(1L);
    }

    @Test
    void getBorrowById_NullId_ShouldReturnNull() { //NoSonar
        when(borrowRepository.findById(null)).thenReturn(Optional.empty());

        Borrow result = borrowService.getBorrowById(null);

        assertNull(result);
        verify(borrowRepository).findById(null);
    }

    @Test
    void getBorrowDemandsByUserEmail_ShouldReturnBorrows() { //NoSonar
        List<Borrow> borrows = Arrays.asList(borrow);
        when(borrowRepository.findBorrowDemandsByOwnerEmail("borrower@example.com")).thenReturn(borrows);

        List<Borrow> result = borrowService.getBorrowDemandsByUserEmail("borrower@example.com");

        assertEquals(1, result.size());
        verify(borrowRepository).findBorrowDemandsByOwnerEmail("borrower@example.com");
    }




    @Test
    void getBorrowDemandsByUserEmail_NonExistentEmail_ShouldReturnEmptyList() { //NoSonar
        when(borrowRepository.findBorrowDemandsByOwnerEmail("unknown@example.com")).thenReturn(Collections.emptyList());

        List<Borrow> result = borrowService.getBorrowDemandsByUserEmail("unknown@example.com");

        assertTrue(result.isEmpty());
        verify(borrowRepository).findBorrowDemandsByOwnerEmail("unknown@example.com");
    }

    @Test
    void getBorrowRequestsByUserEmail_ShouldReturnBorrows() { //NoSonar
        List<Borrow> borrows = Arrays.asList(borrow);
        when(borrowRepository.getBorrowRequestsByUserEmail("borrower@example.com")).thenReturn(borrows);

        List<Borrow> result = borrowService.getBorrowRequestsByUserEmail("borrower@example.com");

        assertEquals(1, result.size());
        verify(borrowRepository).getBorrowRequestsByUserEmail("borrower@example.com");
    }

    @Test
    void getBorrowRequestsByUserEmail_NonExistentEmail_ShouldReturnEmptyList() { //NoSonar
        when(borrowRepository.getBorrowRequestsByUserEmail("unknown@example.com")).thenReturn(Collections.emptyList());

        List<Borrow> result = borrowService.getBorrowRequestsByUserEmail("unknown@example.com");

        assertTrue(result.isEmpty());
        verify(borrowRepository).getBorrowRequestsByUserEmail("unknown@example.com");
    }

    @Test
    void getBorrowStatusUserByEmail_ShouldReturnStats() { //NoSonar
        when(borrowRepository.getTotalApprovedRequestByEmail("borrower@example.com")).thenReturn(1);
        when(borrowRepository.getTotalPendingRequestByEmail("borrower@example.com")).thenReturn(2);
        when(borrowRepository.getTotalProgressRequestByEmail("borrower@example.com")).thenReturn(3);
        when(borrowRepository.getTotalRejectRequestByEmail("borrower@example.com")).thenReturn(4);
        when(borrowRepository.getTotalReturnedRequestByEmail("borrower@example.com")).thenReturn(5);

        BorrowStatusUser result = borrowService.getBorrowStatusUserByEmail("borrower@example.com");

        assertEquals(1, result.getApproved());
        assertEquals(2, result.getPending());
        assertEquals(3, result.getProgress());
        assertEquals(4, result.getRejected());
        assertEquals(5, result.getReturned());
    }



    @Test
    void getBorrowStatusUserByEmail_NonExistentEmail_ShouldReturnZeroedStats() { //NoSonar
        when(borrowRepository.getTotalApprovedRequestByEmail("unknown@example.com")).thenReturn(0);
        when(borrowRepository.getTotalPendingRequestByEmail("unknown@example.com")).thenReturn(0);
        when(borrowRepository.getTotalProgressRequestByEmail("unknown@example.com")).thenReturn(0);
        when(borrowRepository.getTotalRejectRequestByEmail("unknown@example.com")).thenReturn(0);
        when(borrowRepository.getTotalReturnedRequestByEmail("unknown@example.com")).thenReturn(0);

        BorrowStatusUser result = borrowService.getBorrowStatusUserByEmail("unknown@example.com");

        assertEquals(0, result.getApproved());
        assertEquals(0, result.getPending());
        assertEquals(0, result.getProgress());
        assertEquals(0, result.getRejected());
        assertEquals(0, result.getReturned());
    }

    @Test
    void getBookOccupiedDatesByBookId_ShouldReturnDates() { //NoSonar
        List<OccupiedDates> dates = Arrays.asList(new OccupiedDates());
        when(borrowRepository.getBookOccupiedDatesByBookId(1L)).thenReturn(dates);

        List<OccupiedDates> result = borrowService.getBookOccupiedDatesByBookId(1L);

        assertEquals(1, result.size());
        verify(borrowRepository).getBookOccupiedDatesByBookId(1L);
    }


    @Test
    void getBookOccupiedDatesByBookId_NegativeId_ShouldReturnEmptyList() { //NoSonar
        when(borrowRepository.getBookOccupiedDatesByBookId(-1L)).thenReturn(Collections.emptyList());

        List<OccupiedDates> result = borrowService.getBookOccupiedDatesByBookId(-1L);

        assertTrue(result.isEmpty());
        verify(borrowRepository).getBookOccupiedDatesByBookId(-1L);
    }

    @Test
    void getBookOccupiedDatesByBookId_NonExistentId_ShouldReturnEmptyList() { //NoSonar
        when(borrowRepository.getBookOccupiedDatesByBookId(999L)).thenReturn(Collections.emptyList());

        List<OccupiedDates> result = borrowService.getBookOccupiedDatesByBookId(999L);

        assertTrue(result.isEmpty());
        verify(borrowRepository).getBookOccupiedDatesByBookId(999L);
    }

    @Test
    void updateBorrowStatuses_ShouldUpdateApprovedToInProgress() { //NoSonar
        borrow.setBorrowStatus(BorrowStatus.APPROVED);
        List<Borrow> borrows = Arrays.asList(borrow);
        when(borrowRepository.findApprovedBorrowsToday()).thenReturn(borrows);
        when(borrowRepository.saveAll(borrows)).thenReturn(borrows);

        borrowService.updateBorrowStatuses();

        assertEquals(BorrowStatus.IN_PROGRESS, borrow.getBorrowStatus());
        verify(borrowRepository).findApprovedBorrowsToday();
        verify(borrowRepository).saveAll(borrows);
    }



    @Test
    void updateBorrowStatuses_RepositorySaveFails_ShouldThrowException() { //NoSonar
        borrow.setBorrowStatus(BorrowStatus.APPROVED);
        List<Borrow> borrows = Arrays.asList(borrow);
        when(borrowRepository.findApprovedBorrowsToday()).thenReturn(borrows);
        when(borrowRepository.saveAll(borrows)).thenThrow(new RuntimeException("Save failed"));

        assertThrows(RuntimeException.class, () -> borrowService.updateBorrowStatuses());
        verify(borrowRepository).findApprovedBorrowsToday();
        verify(borrowRepository).saveAll(borrows);
    }

    @Test
    void updateBorrowStatusesToReturned_ShouldUpdateProgressToReturned() { //NoSonar
        borrow.setBorrowStatus(BorrowStatus.IN_PROGRESS);
        List<Borrow> borrows = Arrays.asList(borrow);
        when(borrowRepository.findProgressBorrowsEndToday()).thenReturn(borrows);
        when(borrowRepository.saveAll(borrows)).thenReturn(borrows);

        borrowService.updateBorrowStatusesToReturned();

        assertEquals(BorrowStatus.RETURNED, borrow.getBorrowStatus());
        verify(borrowRepository).findProgressBorrowsEndToday();
        verify(borrowRepository).saveAll(borrows);
    }



    @Test
    void updateBorrowStatusesToReturned_RepositorySaveFails_ShouldThrowException() { //NoSonar
        borrow.setBorrowStatus(BorrowStatus.IN_PROGRESS);
        List<Borrow> borrows = Arrays.asList(borrow);
        when(borrowRepository.findProgressBorrowsEndToday()).thenReturn(borrows);
        when(borrowRepository.saveAll(borrows)).thenThrow(new RuntimeException("Save failed"));

        assertThrows(RuntimeException.class, () -> borrowService.updateBorrowStatusesToReturned());
        verify(borrowRepository).findProgressBorrowsEndToday();
        verify(borrowRepository).saveAll(borrows);
    }

    @Test
    void cancelPendingOrApproved_ShouldDeleteBorrow() { //NoSonar
        borrowService.cancelPendingOrApproved(1L);

        verify(borrowRepository).deleteById(1L);
    }

    @Test
    void cancelPendingOrApproved_NonExistentId_ShouldNotThrowException() { //NoSonar
        doNothing().when(borrowRepository).deleteById(999L);

        assertDoesNotThrow(() -> borrowService.cancelPendingOrApproved(999L));
        verify(borrowRepository).deleteById(999L);
    }


    @Test
    void getBookOccupiedDatesUpdatedBorrow_ShouldReturnDates() { //NoSonar
        List<OccupiedDates> dates = Arrays.asList(new OccupiedDates());
        when(borrowRepository.getBookOccupiedDatesByBookIdForUpdatedBorrow(1L, 1L)).thenReturn(dates);

        List<OccupiedDates> result = borrowService.getBookOccupiedDatesUpdatedBorrow(1L, 1L);

        assertEquals(1, result.size());
        verify(borrowRepository).getBookOccupiedDatesByBookIdForUpdatedBorrow(1L, 1L);
    }



    @Test
    void getBookOccupiedDatesUpdatedBorrow_NonExistentIds_ShouldReturnEmptyList() { //NoSonar
        when(borrowRepository.getBookOccupiedDatesByBookIdForUpdatedBorrow(999L, 999L)).thenReturn(Collections.emptyList());

        List<OccupiedDates> result = borrowService.getBookOccupiedDatesUpdatedBorrow(999L, 999L);

        assertTrue(result.isEmpty());
        verify(borrowRepository).getBookOccupiedDatesByBookIdForUpdatedBorrow(999L, 999L);
    }

    @Test
    void updateBorrowWhilePending_ShouldUpdateAndSave() { //NoSonar
        when(userRepository.findByEmail("borrower@example.com")).thenReturn(Optional.of(borrower));
        when(borrowRepository.save(any(Borrow.class))).thenReturn(borrow);

        Borrow result = borrowService.updateBorrowWhilePending(borrow);

        assertEquals(LocalDate.now(), result.getRequestDate());
        assertEquals(BorrowStatus.PENDING, result.getBorrowStatus());
        assertEquals(borrower.getId(), result.getBorrower().getId());
        verify(borrowRepository).save(borrow);
        verify(socketIOService).sendDemandNotification(borrow.getId());
    }

    @Test
    void updateBorrowWhilePending_NullBorrow_ShouldThrowException() { //NoSonar
        assertThrows(NullPointerException.class, () -> borrowService.updateBorrowWhilePending(null));
        verify(userRepository, never()).findByEmail(any());
        verify(borrowRepository, never()).save(any());
        verify(socketIOService, never()).sendDemandNotification(any());
    }

    @Test
    void updateBorrowWhilePending_NonExistentUser_ShouldSaveWithoutIdUpdate() { //NoSonar
        when(userRepository.findByEmail("borrower@example.com")).thenReturn(Optional.empty());
        when(borrowRepository.save(any(Borrow.class))).thenReturn(borrow);

        Borrow result = borrowService.updateBorrowWhilePending(borrow);

        assertEquals(LocalDate.now(), result.getRequestDate());
        assertEquals(BorrowStatus.PENDING, result.getBorrowStatus());
        assertEquals(borrower.getId(), result.getBorrower().getId()); // ID unchanged
        verify(borrowRepository).save(borrow);
        verify(socketIOService).sendDemandNotification(borrow.getId());
    }

    @Test
    void updateBorrowWhilePending_RepositorySaveFails_ShouldThrowException() { //NoSonar
        when(userRepository.findByEmail("borrower@example.com")).thenReturn(Optional.of(borrower));
        when(borrowRepository.save(any(Borrow.class))).thenThrow(new RuntimeException("Save failed"));

        assertThrows(RuntimeException.class, () -> borrowService.updateBorrowWhilePending(borrow));
        verify(borrowRepository).save(borrow);
    }

    @Test
    void markAsReturned_ShouldUpdateStatusToReturned() { //NoSonar
        when(borrowRepository.findById(1L)).thenReturn(Optional.of(borrow));
        when(borrowRepository.save(any(Borrow.class))).thenReturn(borrow);

        borrowService.markAsReturned(1L);

        assertEquals(BorrowStatus.RETURNED, borrow.getBorrowStatus());
        verify(borrowRepository).findById(1L);
        verify(borrowRepository).save(borrow);
    }





    @Test
    void markAsReturned_RepositorySaveFails_ShouldThrowException() { //NoSonar
        when(borrowRepository.findById(1L)).thenReturn(Optional.of(borrow));
        when(borrowRepository.save(any(Borrow.class))).thenThrow(new RuntimeException("Save failed"));

        assertThrows(RuntimeException.class, () -> borrowService.markAsReturned(1L));
        verify(borrowRepository).findById(1L);
        verify(borrowRepository).save(borrow);
    }

    @Test
    void cancelWhileInProgress_ShouldUpdateStatusAndDate() { //NoSonar
        when(borrowRepository.findById(1L)).thenReturn(Optional.of(borrow));
        when(borrowRepository.save(any(Borrow.class))).thenReturn(borrow);

        borrowService.cancelWhileInProgress(1L);

        assertEquals(BorrowStatus.RETURNED, borrow.getBorrowStatus());
        assertEquals(LocalDate.now(), borrow.getExpectedReturnDate());
        verify(borrowRepository).findById(1L);
        verify(borrowRepository).save(borrow);
    }



    @Test
    void cancelWhileInProgress_RepositorySaveFails_ShouldThrowException() { //NoSonar
        when(borrowRepository.findById(1L)).thenReturn(Optional.of(borrow));
        when(borrowRepository.save(any(Borrow.class))).thenThrow(new RuntimeException("Save failed"));

        assertThrows(RuntimeException.class, () -> borrowService.cancelWhileInProgress(1L));
        verify(borrowRepository).findById(1L);
        verify(borrowRepository).save(borrow);
    }
}