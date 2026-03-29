package com.temple.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@RestController
public class FishAudioController {

    @Value("${fish.audio.api.key}")
    private String fishApiKey;

    private static final String VOICE_ID = "db857de9b1be4f9cb53799daaa69218e";

    @PostMapping("/api/fish-tts")
    public ResponseEntity<byte[]> generateSpeech(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + fishApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("text", text);
            body.put("reference_id", VOICE_ID);
            body.put("format", "mp3");
            body.put("latency", "normal");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                "https://api.fish.audio/v1/tts",
                HttpMethod.POST,
                entity,
                byte[].class
            );

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.parseMediaType("audio/mpeg"));

            return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}