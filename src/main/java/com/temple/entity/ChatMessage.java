package com.temple.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;

    @Column(nullable = false)
    private String userMessage;

    @Column(columnDefinition = "TEXT")
    private String botResponse;

    private String intent; // GUIDANCE, EMOTIONAL, ACTION, INFO

    private String language; // tamil, english

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // ── Getters & Setters ────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getBotResponse() { return botResponse; }
    public void setBotResponse(String botResponse) {
        this.botResponse = botResponse;
    }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) {
        this.language = language;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}