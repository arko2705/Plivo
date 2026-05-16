package com.example.demo.config;

import com.plivo.api.Plivo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes the Plivo SDK with credentials from application.properties.
 */
@Configuration
public class PlivoConfig {

    @Value("${plivo.auth.id}")
    private String authId;

    @Value("${plivo.auth.token}")
    private String authToken;

    @PostConstruct
    public void initPlivo() {
        Plivo.init(authId, authToken);
        System.out.println("Plivo SDK initialized with Auth ID: " + authId.substring(0, 4) + "...");
    }

    public String getAuthId() {
        return authId;
    }

    public String getAuthToken() {
        return authToken;
    }
}
