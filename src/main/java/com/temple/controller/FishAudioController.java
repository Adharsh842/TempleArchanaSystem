package com.temple.controller;

import com.temple.entity.Booking;
import com.temple.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RestTemplate restTemplate;

    // ── Called by scanner.html: POST /api/fish-audio/{bookingId}?lang=tamil ──
    @PostMapping("/api/fish-audio/{bookingId}")
    public ResponseEntity<Map<String, Object>> generateFishAudio(
            @PathVariable String bookingId,
            @RequestParam(defaultValue = "tamil") String lang) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Get booking details
            Optional<Booking> bookingOpt = bookingService.findByBookingId(bookingId);
            if (!bookingOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Booking not found");
                return ResponseEntity.ok(response);
            }

            Booking booking = bookingOpt.get();

            // Build archana script
            String script = buildScript(booking, lang);

            // Call Fish Audio API → get MP3 bytes
            byte[] audioBytes = callFishAudio(script);

            if (audioBytes == null || audioBytes.length == 0) {
                response.put("success", false);
                response.put("message", "Fish Audio API failed");
                return ResponseEntity.ok(response);
            }

            // Return audio as base64 so frontend can play directly
            String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
            response.put("success", true);
            response.put("script", script);
            response.put("audioBase64", base64Audio);
            response.put("audioPath", "/api/fish-audio/stream/" + bookingId + "?lang=" + lang);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    // ── Stream audio directly (used by audio src) ──
    @PostMapping("/api/fish-audio/stream/{bookingId}")
    public ResponseEntity<byte[]> streamFishAudio(
            @PathVariable String bookingId,
            @RequestParam(defaultValue = "tamil") String lang) {

        try {
            Optional<Booking> bookingOpt = bookingService.findByBookingId(bookingId);
            if (!bookingOpt.isPresent()) return ResponseEntity.notFound().build();

            Booking booking = bookingOpt.get();
            String script = buildScript(booking, lang);
            byte[] audioBytes = callFishAudio(script);

            if (audioBytes == null) return ResponseEntity.status(500).build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
            headers.setContentLength(audioBytes.length);

            return new ResponseEntity<>(audioBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ── Also keep /api/fish-tts for voice.js ──
    @PostMapping("/api/fish-tts")
    public ResponseEntity<byte[]> fishTts(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            byte[] audioBytes = callFishAudio(text);
            if (audioBytes == null) return ResponseEntity.status(500).build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
            return new ResponseEntity<>(audioBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ── Call Fish Audio API ──
    private byte[] callFishAudio(String text) {
        try {
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

            return response.getBody();

        } catch (Exception e) {
            System.err.println("Fish Audio API error: " + e.getMessage());
            return null;
        }
    }

    // ── Build Tamil Script ──
    private String buildScript(Booking booking, String lang) {
        String name   = booking.getDevoteeName()        != null ? booking.getDevoteeName()        : "";
        String raasi  = booking.getDevoteeRaasi()       != null ? booking.getDevoteeRaasi()       : "";
        String naksha = booking.getDevoteeNakshatram()  != null ? booking.getDevoteeNakshatram()  : "";
        String archana= booking.getArchanaType()        != null ? booking.getArchanaType()        : "";

        if ("tamil".equals(lang)) {
            return "Om. Om. Om. Shubham Karodi Kalyanam. Aarogyam. Thana Sampatha. " +
                   "Adiyargale. " + name + " avargalukku. " +
                   raasi + " raasi. " + naksha + " natchathiram. " +
                   getTamilChant(archana) +
                   "Sarva Mangalam Piraapthirastu. Aayu. Arokyam. Aishwaryam. " +
                   "Om Shaanthi. Om Shaanthi. Om Shaanthi. " +
                   "Jai Sri Venkadaeshwara. Govindaa. Govindaa. Govindaa.";
        } else {
            return "Om. Om. Om. Shubham Karoti Kalyanam. " +
                   name + " avargalukku. " +
                   raasi + " raasi. " + naksha + " nakshatram. " +
                   getEngChant(archana) +
                   "Sarva Mangala Prapthirastu. Om Shanti. Om Shanti. Om Shanti. " +
                   "Jai Sri Venkateswara. Govinda. Govinda. Govinda.";
        }
    }

    private String getTamilChant(String type) {
        switch (type) {
            case "Pushparchana":    return "Pushpa Archana Samarppanam. Om Namo Narayanaya. ";
            case "Kumkumarchana":   return "Kumkuma Archana Samarppanam. Om Sakthi Namaha. ";
            case "Abhishekam":      return "Thiruvabishekam Samarppanam. Om Namah Shivaya. ";
            case "Sahasranamam":    return "Sahasranama Archana Samarppanam. Om Namo Bhagavathe Vasudevaaya. ";
            case "Ganapathi Homam": return "Ganapathi Homam Samarppanam. Om Gam Ganapataye Namaha. ";
            default:                return "Archana Samarppanam. Om Namo Narayanaya. ";
        }
    }

    private String getEngChant(String type) {
        switch (type) {
            case "Pushparchana":    return "Pushpa Archana samarpanam. Om Namo Narayanaya. ";
            case "Kumkumarchana":   return "Kumkuma Archana samarpanam. Om Shakti Namaha. ";
            case "Abhishekam":      return "Abhishekam samarpanam. Om Namah Shivaya. ";
            case "Sahasranamam":    return "Sahasranama samarpanam. Om Namo Vasudevaya. ";
            case "Ganapathi Homam": return "Ganapathi Homam. Om Gam Ganapataye Namaha. ";
            default:                return "Archana samarpanam. Om Namo Narayanaya. ";
        }
    }
}