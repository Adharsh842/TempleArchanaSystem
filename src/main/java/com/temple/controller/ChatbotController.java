package com.temple.controller;

import com.temple.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Controller
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    // ── Chatbot Page ──────────────────────────────────
    @GetMapping("/chatbot")
    public String chatbotPage() {
        return "chatbot";
    }

    // ── Process Message ───────────────────────────────
    @PostMapping("/api/chat")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> chat(
            @RequestParam String message,
            @RequestParam(required = false)
                String sessionId,
            @RequestParam(defaultValue = "english")
                String language) {

        // Generate session ID if not provided
        if (sessionId == null ||
            sessionId.trim().isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        if (message == null ||
            message.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> result =
            chatbotService.processMessage(
                message.trim(),
                sessionId,
                language);

        return ResponseEntity.ok(result);
    }
}