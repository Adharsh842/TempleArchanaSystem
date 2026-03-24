package com.temple.controller;

import com.temple.entity.Booking;
import com.temple.service.ArchanaVoiceService;
import com.temple.service.BookingService;
import com.temple.service.FreeTTSService;
import com.temple.service.FishAudioService;
import com.temple.service.TamilVoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class QRCodeController {

    // ── SERVICES ─────────────────────────────────────────────────
    @Autowired
    private BookingService bookingService;

    @Autowired
    private ArchanaVoiceService archanaVoiceService;

    @Autowired
    private TamilVoiceService tamilVoiceService;

    @Autowired
    private FreeTTSService freeTTSService;

    @Autowired
    private FishAudioService fishAudioService;

    // ═══════════════════════════════════════════════════════════════
    // API 1 — VERIFY BOOKING
    // URL: GET /api/verify/{bookingId}
    // Called by QR scanner when devotee arrives at temple
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/verify/{bookingId}")
    public ResponseEntity<Map<String, Object>> verifyBooking(
            @PathVariable String bookingId) {

        Map<String, Object> response = new HashMap<>();

        // Validate input
        if (bookingId == null || bookingId.trim().isEmpty()) {
            response.put("success",         false);
            response.put("alreadyVerified", false);
            response.put("message",         "Booking ID cannot be empty.");
            response.put("archanaScript",   "");
            response.put("quickScript",     "");
            response.put("tamilScript",     "");
            response.put("tamilAudioUrl",   "");
            return ResponseEntity.ok(response);
        }

        String cleanId = bookingId.trim();

        // Find booking
        Optional<Booking> bookingOpt =
            bookingService.findByBookingId(cleanId);

        // Not found
        if (bookingOpt.isEmpty()) {
            response.put("success",         false);
            response.put("alreadyVerified", false);
            response.put("message",
                "Booking NOT FOUND: " + cleanId);
            response.put("archanaScript",   "");
            response.put("quickScript",     "");
            response.put("tamilScript",     "");
            response.put("tamilAudioUrl",   "");
            return ResponseEntity.ok(response);
        }

        Booking booking    = bookingOpt.get();
        String devoteeName = booking.getDevotee().getName();
        String gothram     = booking.getDevotee().getGothram();
        String nakshatram  = booking.getDevotee().getNakshatram();
        String archanaType = booking.getArchanaType();
        String timeSlot    = booking.getTimeSlot();

        // Build archana scripts
        String archanaScript = buildFallbackScript(
            devoteeName, gothram, nakshatram, archanaType);
        try {
            archanaScript = archanaVoiceService.buildArchanaScript(
                devoteeName, gothram, nakshatram, archanaType);
        } catch (Exception e) {
            System.err.println("ArchanaScript error: "
                + e.getMessage());
        }

        String quickScript = devoteeName +
            " avargalukku. " + archanaType +
            " confirmed. Govinda.";
        try {
            quickScript = archanaVoiceService.buildQuickAnnouncement(
                devoteeName, gothram, nakshatram,
                archanaType, timeSlot);
        } catch (Exception e) {
            System.err.println("QuickScript error: "
                + e.getMessage());
        }

        String tamilScript = devoteeName +
            " avargalukku. " + archanaType +
            " samarppanam. Govindaa.";
        try {
            tamilScript = tamilVoiceService
                .buildTamilTransliterationText(
                    devoteeName, gothram,
                    nakshatram, archanaType);
        } catch (Exception e) {
            System.err.println("TamilScript error: "
                + e.getMessage());
        }

        String tamilAudioUrl = "";
        try {
            String tamilText = freeTTSService.buildTamilText(
                devoteeName, gothram, nakshatram, archanaType);
            tamilAudioUrl = freeTTSService
                .buildTamilAudioUrl(tamilText);
        } catch (Exception e) {
            System.err.println("TamilAudioUrl error: "
                + e.getMessage());
        }

        // Already verified
        if (Boolean.TRUE.equals(booking.getIsVerified())) {
            response.put("success",         false);
            response.put("alreadyVerified", true);
            response.put("message",
                "Already verified at " +
                booking.getVerifiedAt());
            response.put("booking",
                buildBookingMap(booking));
            response.put("archanaScript",   archanaScript);
            response.put("quickScript",     quickScript);
            response.put("tamilScript",     tamilScript);
            response.put("tamilAudioUrl",   tamilAudioUrl);

            System.out.println("Booking already verified: "
                + cleanId + " at "
                + booking.getVerifiedAt());
            return ResponseEntity.ok(response);
        }

        // Mark as verified
        Booking verified;
        try {
            verified = bookingService.verifyBooking(cleanId);
            System.out.println("Booking verified successfully: "
                + cleanId);
        } catch (Exception e) {
            response.put("success",         false);
            response.put("alreadyVerified", false);
            response.put("message",
                "Verification failed: " + e.getMessage());
            response.put("archanaScript",   archanaScript);
            response.put("quickScript",     quickScript);
            response.put("tamilScript",     tamilScript);
            response.put("tamilAudioUrl",   tamilAudioUrl);
            return ResponseEntity.ok(response);
        }

        // Success response
        response.put("success",         true);
        response.put("alreadyVerified", false);
        response.put("message",         "Booking VERIFIED successfully!");
        response.put("booking",         buildBookingMap(verified));
        response.put("archanaScript",   archanaScript);
        response.put("quickScript",     quickScript);
        response.put("tamilScript",     tamilScript);
        response.put("tamilAudioUrl",   tamilAudioUrl);

        System.out.println("Verify API response sent for: "
            + cleanId);
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════════
    // API 2 — GET BOOKING DETAILS
    // URL: GET /api/booking/{bookingId}
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Map<String, Object>> getBookingDetails(
            @PathVariable String bookingId) {

        Map<String, Object> response = new HashMap<>();

        Optional<Booking> bookingOpt =
            bookingService.findByBookingId(
                bookingId != null ? bookingId.trim() : "");

        if (bookingOpt.isEmpty()) {
            response.put("success", false);
            response.put("message",
                "Booking not found: " + bookingId);
            return ResponseEntity.ok(response);
        }

        Booking booking    = bookingOpt.get();
        String devoteeName = booking.getDevotee().getName();
        String gothram     = booking.getDevotee().getGothram();
        String nakshatram  = booking.getDevotee().getNakshatram();
        String archanaType = booking.getArchanaType();

        String archanaScript = buildFallbackScript(
            devoteeName, gothram, nakshatram, archanaType);
        String tamilScript   = devoteeName + " avargalukku. ";

        try {
            archanaScript = archanaVoiceService.buildArchanaScript(
                devoteeName, gothram, nakshatram, archanaType);
            tamilScript = tamilVoiceService
                .buildTamilTransliterationText(
                    devoteeName, gothram,
                    nakshatram, archanaType);
        } catch (Exception e) {
            System.err.println("Script error: " + e.getMessage());
        }

        response.put("success",       true);
        response.put("booking",       buildBookingMap(booking));
        response.put("archanaScript", archanaScript);
        response.put("tamilScript",   tamilScript);

        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════════
    // API 3 — CHECK BOOKING STATUS
    // URL: GET /api/status/{bookingId}
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/status/{bookingId}")
    public ResponseEntity<Map<String, Object>> checkStatus(
            @PathVariable String bookingId) {

        Map<String, Object> response = new HashMap<>();

        Optional<Booking> bookingOpt =
            bookingService.findByBookingId(
                bookingId != null ? bookingId.trim() : "");

        if (bookingOpt.isEmpty()) {
            response.put("found",    false);
            response.put("verified", false);
            response.put("message",  "Booking not found");
        } else {
            Booking b = bookingOpt.get();
            response.put("found",       true);
            response.put("verified",    b.getIsVerified());
            response.put("bookingId",   b.getBookingId());
            response.put("devoteeName", b.getDevotee().getName());
            response.put("archanaType", b.getArchanaType());
            response.put("verifiedAt",
                b.getVerifiedAt() != null
                    ? b.getVerifiedAt().toString() : null);
        }

        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════════
    // API 4 — FREE GOOGLE TRANSLATE TTS
    // URL: POST /api/free-audio/{bookingId}?lang=tamil
    // No API key needed — completely free
    // ═══════════════════════════════════════════════════════════════
    @PostMapping("/free-audio/{bookingId}")
    public ResponseEntity<Map<String, Object>> generateFreeAudio(
            @PathVariable String bookingId,
            @RequestParam(defaultValue = "tamil") String lang) {

        Map<String, Object> response = new HashMap<>();

        Optional<Booking> bookingOpt =
            bookingService.findByBookingId(bookingId);

        if (bookingOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Booking not found");
            return ResponseEntity.ok(response);
        }

        Booking booking = bookingOpt.get();
        String  name    = booking.getDevotee().getName();
        String  gothram = booking.getDevotee().getGothram();
        String  naksha  = booking.getDevotee().getNakshatram();
        String  archana = booking.getArchanaType();

        try {
            String text;
            String audioPath;

            if ("tamil".equals(lang)) {
                text = freeTTSService.buildTamilText(
                    name, gothram, naksha, archana);
                audioPath = freeTTSService.generateTamilAudio(
                    text, bookingId);
            } else {
                text = freeTTSService.buildEnglishText(
                    name, gothram, naksha, archana);
                audioPath = freeTTSService.generateEnglishAudio(
                    text, bookingId);
            }

            response.put("success",   true);
            response.put("audioPath", audioPath);
            response.put("language",  lang);
            response.put("text",      text);

            System.out.println("Free TTS generated: " + audioPath);

        } catch (Exception e) {
            System.err.println("Free TTS error: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════════
    // API 5 — FISH AUDIO PRIEST VOICE
    // URL: POST /api/fish-audio/{bookingId}?lang=tamil
    // Realistic AI priest voice using Fish Audio API
    // ═══════════════════════════════════════════════════════════════
    @PostMapping("/fish-audio/{bookingId}")
    public ResponseEntity<Map<String, Object>> generateFishAudio(
            @PathVariable String bookingId,
            @RequestParam(defaultValue = "tamil") String lang) {

        Map<String, Object> response = new HashMap<>();

        Optional<Booking> bookingOpt =
            bookingService.findByBookingId(bookingId);

        if (bookingOpt.isEmpty()) {
            response.put("success", false);
            response.put("message",
                "Booking not found: " + bookingId);
            return ResponseEntity.ok(response);
        }

        Booking booking = bookingOpt.get();
        String  name    = booking.getDevotee().getName();
        String  gothram = booking.getDevotee().getGothram();
        String  naksha  = booking.getDevotee().getNakshatram();
        String  archana = booking.getArchanaType();

        System.out.println("Fish Audio request: " +
            name + " | " + archana + " | " + lang);

        try {
            // Build priest archana script
            String script = fishAudioService.buildPriestText(
                name, gothram, naksha, archana, lang);

            System.out.println("Fish Audio script length: " +
                script.length() + " chars");

            // Generate audio via Fish Audio API
            String audioPath = fishAudioService
                .generatePriestVoice(script, bookingId, lang);

            response.put("success",   true);
            response.put("audioPath", audioPath);
            response.put("script",    script);
            response.put("language",  lang);
            response.put("devotee",   name);
            response.put("archana",   archana);

            System.out.println("Fish Audio generated: "
                + audioPath);

        } catch (Exception e) {
            System.err.println("Fish Audio error: "
                + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════════
    // API 6 — ALL BOOKINGS SUMMARY
    // URL: GET /api/bookings/all
    // Used by admin dashboard stats
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/bookings/all")
    public ResponseEntity<Map<String, Object>> getAllBookingsSummary() {

        Map<String, Object> response = new HashMap<>();

        try {
            List<Booking> allBookings =
                bookingService.getAllBookings();

            long totalCount = allBookings.size();
            long todayCount = bookingService
                .getTodayBookingCount();
            long verifiedCount = allBookings.stream()
                .filter(b ->
                    Boolean.TRUE.equals(b.getIsVerified()))
                .count();

            response.put("success",       true);
            response.put("totalCount",    totalCount);
            response.put("todayCount",    todayCount);
            response.put("verifiedCount", verifiedCount);
            response.put("pendingCount",
                totalCount - verifiedCount);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPER — Build booking map for JSON response
    // ═══════════════════════════════════════════════════════════════
    private Map<String, Object> buildBookingMap(Booking booking) {
        Map<String, Object> map = new HashMap<>();

        map.put("bookingId",     booking.getBookingId());
        map.put("archanaType",   booking.getArchanaType());
        map.put("timeSlot",      booking.getTimeSlot());
        map.put("bookingDate",
            booking.getBookingDate() != null
                ? booking.getBookingDate().toString() : "");
        map.put("amount",        booking.getAmount());
        map.put("paymentStatus", booking.getPaymentStatus());
        map.put("bookingStatus", booking.getBookingStatus());
        map.put("isVerified",    booking.getIsVerified());
        map.put("verifiedAt",
            booking.getVerifiedAt() != null
                ? booking.getVerifiedAt().toString() : null);
        map.put("qrCodePath",    booking.getQrCodePath());

        if (booking.getDevotee() != null) {
            map.put("devoteeName",
                booking.getDevotee().getName());
            map.put("gothram",
                booking.getDevotee().getGothram());
            map.put("nakshatram",
                booking.getDevotee().getNakshatram());
            map.put("phone",
                booking.getDevotee().getPhone());
            map.put("email",
                booking.getDevotee().getEmail() != null
                    ? booking.getDevotee().getEmail() : "");
        } else {
            map.put("devoteeName", "Unknown");
            map.put("gothram",     "Unknown");
            map.put("nakshatram",  "Unknown");
            map.put("phone",       "");
            map.put("email",       "");
        }

        return map;
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPER — Fallback script if services unavailable
    // ═══════════════════════════════════════════════════════════════
    private String buildFallbackScript(
            String name, String gothram,
            String nakshatram, String archanaType) {

        return "Om Namah. " +
               name        + " avargalukku. " +
               gothram     + " gothram. " +
               nakshatram  + " nakshatram. " +
               archanaType + " samarpanam. " +
               "Sarva Mangala Prapthirastu. " +
               "Govinda Govinda.";
    }
}