package com.sofrecom.backend.config;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SocketIOConfig {

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        //config.setHostname("localhost");
        config.setHostname("0.0.0.0");
        config.setPort(9092); // Port for Socket.IO
        config.setOrigin("*"); // autoriser toutes les origines
        return new SocketIOServer(config);
    }
}