package com.backend.tutor_app.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FireBaseConfig {

    @Value("${firebase.config.path:firebase/tutorapp-81992-firebase-adminsdk-fbsvc-0c37260092.json}")
    private String firebaseConfigPath;

    @Value("${firebase.database.url:https://tutorapp-81992-default-rtdb.firebaseio.com}")
    private String firebaseDatabaseUrl;

    @Value("${firebase.project.id:tutorapp-81992}")
    private String firebaseProjectId;

    @PostConstruct
    public void initialize() {
        log.info("🔥 Starting Firebase initialization...");
        log.info("Config path: {}", firebaseConfigPath);
        log.info("Database URL: {}", firebaseDatabaseUrl);
        log.info("Project ID: {}", firebaseProjectId);
        
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
                
                if (!resource.exists()) {
                    log.error("❌ Firebase config file not found at: {}", firebaseConfigPath);
                    log.warn("⚠️ Firebase initialization skipped - file not found");
                    return;
                }
                
                log.info("✅ Firebase config file found, loading credentials...");
                
                try (InputStream serviceAccount = resource.getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .setDatabaseUrl(firebaseDatabaseUrl)
                            .setProjectId(firebaseProjectId)
                            .build();

                    FirebaseApp.initializeApp(options);
                    log.info("🎉 Firebase Admin SDK initialized successfully");
                    log.info("📱 Project ID: {}", firebaseProjectId);
                    log.info("🔗 Database URL: {}", firebaseDatabaseUrl);
                }
            } else {
                log.info("ℹ️ Firebase Admin SDK already initialized");
            }
        } catch (Exception e) {
            log.error("❌ Failed to initialize Firebase Admin SDK: {}", e.getMessage());
            log.error("📋 Stack trace:", e);
            log.warn("⚠️ Firebase initialization failed - continuing without Firebase");
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        return FirebaseMessaging.getInstance();
    }
}
