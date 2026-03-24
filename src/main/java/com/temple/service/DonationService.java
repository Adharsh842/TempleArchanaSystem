package com.temple.service;

import com.temple.entity.Donation;
import com.temple.repository.DonationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class DonationService {

    @Autowired
    private DonationRepository donationRepository;

    // ════════════════════════════════════════════════
    // CREATE DONATION
    // ════════════════════════════════════════════════
    public Donation createDonation(
            String donorName,
            String phone,
            String email,
            BigDecimal amount,
            String donationType,
            String message) {

        Donation donation = new Donation();
        donation.setDonorName(donorName.trim());
        donation.setPhone(phone.trim());
        donation.setEmail(email != null
            ? email.trim() : "");
        donation.setAmount(amount);
        donation.setDonationType(donationType);
        donation.setMessage(message != null
            ? message.trim() : "");
        donation.setStatus("COMPLETED");
        donation.setReceiptNumber(generateReceiptNumber());
        donation.setDonatedAt(LocalDateTime.now());

        Donation saved = donationRepository.save(donation);
        System.out.println("Donation saved: " +
            saved.getReceiptNumber() +
            " | ₹" + saved.getAmount());
        return saved;
    }

    // ════════════════════════════════════════════════
    // GET ALL DONATIONS
    // ════════════════════════════════════════════════
    public List<Donation> getAllDonations() {
        return donationRepository
            .findAllByOrderByDonatedAtDesc();
    }

    // ════════════════════════════════════════════════
    // GET TOTAL AMOUNT
    // ════════════════════════════════════════════════
    public BigDecimal getTotalDonations() {
        BigDecimal total =
            donationRepository.getTotalDonations();
        return total != null ? total : BigDecimal.ZERO;
    }

    // ════════════════════════════════════════════════
    // FIND BY RECEIPT
    // ════════════════════════════════════════════════
    public Donation findByReceipt(String receipt) {
        return donationRepository
            .findByReceiptNumber(receipt)
            .orElse(null);
    }

    // ════════════════════════════════════════════════
    // GENERATE RECEIPT NUMBER
    // Format: DON-20260321-ABCD
    // ════════════════════════════════════════════════
    private String generateReceiptNumber() {
        String date = LocalDateTime.now()
            .format(DateTimeFormatter
                .ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID()
            .toString()
            .substring(0, 4)
            .toUpperCase();
        return "DON-" + date + "-" + random;
    }

    // ════════════════════════════════════════════════
    // VALIDATE DONATION TYPE
    // ════════════════════════════════════════════════
    public boolean isValidDonationType(String type) {
        return type != null && (
            type.equals("Annadhanam")   ||
            type.equals("Temple Fund")  ||
            type.equals("Special Pooja")||
            type.equals("Cow Donation") ||
            type.equals("Go Pooja")
        );
    }
}