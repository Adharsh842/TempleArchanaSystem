package com.temple.service;

import com.google.zxing.WriterException;
import com.temple.entity.Booking;
import com.temple.entity.Devotee;
import com.temple.repository.BookingRepository;
import com.temple.repository.DevoteeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private DevoteeRepository devoteeRepository;

    @Autowired
    private QRCodeService qrCodeService;

    public BigDecimal getArchanaPrice(String archanaType) {
        if (archanaType == null || archanaType.trim().isEmpty()) {
            return new BigDecimal("10.00");
        }
        switch (archanaType.trim()) {
            case "Pushparchana":    return new BigDecimal("10.00");
            case "Kumkumarchana":   return new BigDecimal("20.00");
            case "Abhishekam":      return new BigDecimal("30.00");
            case "Sahasranamam":    return new BigDecimal("40.00");
            case "Ganapathi Homam": return new BigDecimal("50.00");
            default:                return new BigDecimal("10.00");
        }
    }

    public Booking createBooking(
            String name,
            String raasi,
            String nakshatram,
            String phone,
            String email,
            String archanaType,
            String timeSlot) throws WriterException, IOException {

        System.out.println("Creating booking for: " + name + " | Phone: " + phone);

        Optional<Devotee> existingOpt = devoteeRepository.findByPhone(phone);
        Devotee devotee;

        if (existingOpt.isPresent()) {
            // ✅ Reuse existing devotee — do NOT overwrite their details
            devotee = existingOpt.get();
            System.out.println("Existing devotee found: " + devotee.getName());
            // Only update email if provided
            if (email != null && !email.trim().isEmpty()) {
                devotee.setEmail(email.trim());
                devoteeRepository.save(devotee);
            }
        } else {
            // ✅ New devotee — create fresh record
            devotee = new Devotee();
            devotee.setName(name.trim());
            devotee.setGothram(raasi.trim());
            devotee.setNakshatram(nakshatram.trim());
            devotee.setPhone(phone.trim());
            if (email != null && !email.trim().isEmpty()) {
                devotee.setEmail(email.trim());
            }
            devotee = devoteeRepository.save(devotee);
            System.out.println("New devotee created: " + name);
        }

        // Generate Booking ID
        String timestamp  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String bookingId  = "TEMPLE-" + timestamp + "-" + randomPart;

        // Create Booking
        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setDevotee(devotee);

        // ✅ Snapshot — always save what the user entered right now
        booking.setDevoteeName(name.trim());
        booking.setDevoteeRaasi(raasi.trim());
        booking.setDevoteeNakshatram(nakshatram.trim());

        booking.setArchanaType(archanaType);
        booking.setTimeSlot(timeSlot);
        booking.setAmount(getArchanaPrice(archanaType));
        booking.setPaymentStatus("COMPLETED");
        booking.setBookingStatus("CONFIRMED");
        booking.setIsVerified(false);
        booking.setBookingDate(LocalDate.now());
        booking.setCreatedAt(LocalDateTime.now());

        // QR Content
        String qrContent =
            "BookingID:" + bookingId   + "\n" +
            "Name:"      + name        + "\n" +
            "Raasi:"     + raasi       + "\n" +
            "Naksha:"    + nakshatram  + "\n" +
            "Archana:"   + archanaType + "\n" +
            "Slot:"      + timeSlot;

        try {
            String base64 = qrCodeService.generateQRCodeBase64(bookingId, qrContent);
            booking.setQrCodeBase64(base64);
            booking.setQrCodePath("/qrcodes/QR_" + bookingId + ".png"); // keep for reference
        } catch (Exception e) {
            System.err.println("QR error: " + e.getMessage());
            booking.setQrCodeBase64(null);
        }

        Booking savedBooking = bookingRepository.save(booking);
        System.out.println("Booking saved: " + savedBooking.getId());
        return savedBooking;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Booking> findByBookingId(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) return Optional.empty();
        return bookingRepository.findByBookingId(bookingId.trim());
    }

    public Booking verifyBooking(String bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        booking.setIsVerified(true);
        booking.setVerifiedAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    public long getTodayBookingCount() {
        return bookingRepository.countByBookingDate(LocalDate.now());
    }

    public List<Booking> getUnverifiedBookings() {
        return bookingRepository.findUnverifiedBookings();
    }

    public List<Booking> getBookingsByDate(LocalDate date) {
        return bookingRepository.findByBookingDate(date);
    }
}