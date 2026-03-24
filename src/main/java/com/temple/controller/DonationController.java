package com.temple.controller;

import com.temple.entity.Donation;
import com.temple.service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DonationController {

    @Autowired
    private DonationService donationService;

    // ── Donation Page ─────────────────────────────────
    @GetMapping("/donation")
    public String donationPage(Model model) {
        model.addAttribute("totalDonations",
            donationService.getTotalDonations());
        return "donation";
    }

    // ── Submit Donation ───────────────────────────────
    @PostMapping("/api/donate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> donate(
            @RequestParam String donorName,
            @RequestParam String phone,
            @RequestParam(required = false) String email,
            @RequestParam BigDecimal amount,
            @RequestParam String donationType,
            @RequestParam(required = false)
                String message) {

        Map<String, Object> response = new HashMap<>();

        // Validate
        if (donorName == null ||
            donorName.trim().isEmpty()) {
            response.put("success", false);
            response.put("message",
                "Donor name is required");
            return ResponseEntity.ok(response);
        }

        if (amount == null ||
            amount.compareTo(BigDecimal.ZERO) <= 0) {
            response.put("success", false);
            response.put("message",
                "Valid amount is required");
            return ResponseEntity.ok(response);
        }

        if (!donationService.isValidDonationType(
                donationType)) {
            response.put("success", false);
            response.put("message",
                "Invalid donation type");
            return ResponseEntity.ok(response);
        }

        try {
            Donation saved = donationService.createDonation(
                donorName, phone, email,
                amount, donationType, message);

            response.put("success", true);
            response.put("message",
                "Donation successful! Thank you 🙏");
            response.put("receiptNumber",
                saved.getReceiptNumber());
            response.put("amount",    saved.getAmount());
            response.put("type",      saved.getDonationType());
            response.put("donorName", saved.getDonorName());

            System.out.println("Donation: " +
                saved.getReceiptNumber() +
                " | ₹" + saved.getAmount());

        } catch (Exception e) {
            System.err.println("Donation error: " +
                e.getMessage());
            response.put("success", false);
            response.put("message",
                "Error: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // ── Admin: All Donations ──────────────────────────
    @GetMapping("/api/donations")
    @ResponseBody
    public ResponseEntity<Map<String, Object>>
            getAllDonations() {

        Map<String, Object> response = new HashMap<>();

        try {
            List<Donation> donations =
                donationService.getAllDonations();
            BigDecimal total =
                donationService.getTotalDonations();

            response.put("success",   true);
            response.put("donations", donations);
            response.put("total",     total);
            response.put("count",     donations.size());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}