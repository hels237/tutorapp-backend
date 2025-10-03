package com.backend.tutor_app.dto.common;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String message;
    private LocalDateTime timestamp;
    private String type; // "success", "info", "warning", "error"

    // Constructeurs de convenance
    public MessageResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.type = "success";
    }

    public MessageResponse(String message, String type) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.type = type;
    }

    // Méthodes statiques pour faciliter la création
    public static MessageResponse success(String message) {
        return MessageResponse.builder()
                .message(message)
                .type("success")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static MessageResponse info(String message) {
        return MessageResponse.builder()
                .message(message)
                .type("info")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static MessageResponse warning(String message) {
        return MessageResponse.builder()
                .message(message)
                .type("warning")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static MessageResponse error(String message) {
        return MessageResponse.builder()
                .message(message)
                .type("error")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
