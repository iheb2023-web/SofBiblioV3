package com.sofrecom.backend.services;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocketIOServiceTest {

    @Mock
    private SocketIOServer server;

    @Mock
    private BroadcastOperations broadcastOperations;

    @Mock
    private SocketIOClient socketIOClient;

    @InjectMocks
    private SocketIOService socketIOService;

    @BeforeEach
    void setUp() {
        // No stubbing here to avoid unnecessary stubbing
    }

    @Test
    void start_ShouldAddListenersAndStartServer() { //NOSONAR
        doNothing().when(server).addConnectListener(any());
        doNothing().when(server).addDisconnectListener(any());
        doNothing().when(server).start();

        socketIOService.start();

        verify(server).addConnectListener(any());
        verify(server).addDisconnectListener(any());
        verify(server).start();
    }

    @Test
    void start_ConnectListener_ShouldLogClientConnection() { //NOSONAR
        ArgumentCaptor<com.corundumstudio.socketio.listener.ConnectListener> connectListenerCaptor = ArgumentCaptor.forClass(com.corundumstudio.socketio.listener.ConnectListener.class);
        doNothing().when(server).addConnectListener(connectListenerCaptor.capture());
        doNothing().when(server).addDisconnectListener(any());
        doNothing().when(server).start();

        socketIOService.start();

        // Simulate client connection
        UUID sessionId = UUID.randomUUID();
        when(socketIOClient.getSessionId()).thenReturn(sessionId);
        connectListenerCaptor.getValue().onConnect(socketIOClient);

        // Assert (logging is hard to verify directly, so we ensure the listener is called)
        verify(socketIOClient).getSessionId();
        verify(server).addConnectListener(any());
        verify(server).start();
    }

    @Test
    void start_DisconnectListener_ShouldLogClientDisconnection() { //NOSONAR
        ArgumentCaptor<com.corundumstudio.socketio.listener.DisconnectListener> disconnectListenerCaptor = ArgumentCaptor.forClass(com.corundumstudio.socketio.listener.DisconnectListener.class);
        doNothing().when(server).addConnectListener(any());
        doNothing().when(server).addDisconnectListener(disconnectListenerCaptor.capture());
        doNothing().when(server).start();

        socketIOService.start();

        // Simulate client disconnection
        UUID sessionId = UUID.randomUUID();
        when(socketIOClient.getSessionId()).thenReturn(sessionId);
        disconnectListenerCaptor.getValue().onDisconnect(socketIOClient);

        verify(socketIOClient).getSessionId();
        verify(server).addDisconnectListener(any());
        verify(server).start();
    }



    @Test
    void stop_ShouldStopServer() { //NOSONAR
        doNothing().when(server).stop();

        socketIOService.stop();

        verify(server).stop();
    }


    @Test
    void sendBookUpdateNotification_ShouldBroadcastEvent() { //NOSONAR
        String bookDetails = "Book updated details";
        when(server.getBroadcastOperations()).thenReturn(broadcastOperations);
        doNothing().when(broadcastOperations).sendEvent("bookUpdated", bookDetails);

        socketIOService.sendBookUpdateNotification(bookDetails);

        verify(server).getBroadcastOperations();
        verify(broadcastOperations).sendEvent("bookUpdated", bookDetails);
    }

    @Test
    void sendBookUpdateNotification_NullBookDetails_ShouldBroadcastNull() { //NOSONAR
        when(server.getBroadcastOperations()).thenReturn(broadcastOperations);
        doNothing().when(broadcastOperations).sendEvent("bookUpdated", (String) null);

        socketIOService.sendBookUpdateNotification(null);

        verify(server).getBroadcastOperations();
        verify(broadcastOperations).sendEvent("bookUpdated", (String) null);
    }

    @Test
    void sendProcessBorrowRequestNotification_ShouldBroadcastEvent() { //NOSONAR
        Long borrowId = 1L;
        when(server.getBroadcastOperations()).thenReturn(broadcastOperations);
        doNothing().when(broadcastOperations).sendEvent("processBorrowRequest", borrowId);

        socketIOService.sendProcessBorrowRequestNotification(borrowId);

        verify(server).getBroadcastOperations();
        verify(broadcastOperations).sendEvent("processBorrowRequest", borrowId);
    }

    @Test
    void sendProcessBorrowRequestNotification_NullBorrowId_ShouldBroadcastNull() { //NOSONAR
        when(server.getBroadcastOperations()).thenReturn(broadcastOperations);
        doNothing().when(broadcastOperations).sendEvent("processBorrowRequest", (Long) null);

        socketIOService.sendProcessBorrowRequestNotification(null);

        verify(server).getBroadcastOperations();
        verify(broadcastOperations).sendEvent("processBorrowRequest", (Long) null);
    }

    @Test
    void sendDemandNotification_ShouldBroadcastEvent() { //NOSONAR
        Long borrowId = 1L;
        when(server.getBroadcastOperations()).thenReturn(broadcastOperations);
        doNothing().when(broadcastOperations).sendEvent("processDemand", borrowId);

        socketIOService.sendDemandNotification(borrowId);

        verify(server).getBroadcastOperations();
        verify(broadcastOperations).sendEvent("processDemand", borrowId);
    }

    @Test
    void sendDemandNotification_NullBorrowId_ShouldBroadcastNull() { //NOSONAR
        when(server.getBroadcastOperations()).thenReturn(broadcastOperations);
        doNothing().when(broadcastOperations).sendEvent("processDemand", (Long) null);

        socketIOService.sendDemandNotification(null);

        verify(server).getBroadcastOperations();
        verify(broadcastOperations).sendEvent("processDemand", (Long) null);
    }

    @Test
    void sendAddReviewNotification_ShouldBroadcastEvent() { //NOSONAR
        Long reviewId = 1L;
        when(server.getBroadcastOperations()).thenReturn(broadcastOperations);
        doNothing().when(broadcastOperations).sendEvent("addReview", reviewId);

        socketIOService.sendAddReviewNotification(reviewId);

        verify(server).getBroadcastOperations();
        verify(broadcastOperations).sendEvent("addReview", reviewId);
    }

    @Test
    void sendAddReviewNotification_NullReviewId_ShouldBroadcastNull() { //NOSONAR
        when(server.getBroadcastOperations()).thenReturn(broadcastOperations);
        doNothing().when(broadcastOperations).sendEvent("addReview", (Long) null);

        socketIOService.sendAddReviewNotification(null);

        verify(server).getBroadcastOperations();
        verify(broadcastOperations).sendEvent("addReview", (Long) null);
    }
}