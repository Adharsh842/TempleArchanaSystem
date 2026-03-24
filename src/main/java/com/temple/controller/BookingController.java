package com.temple.controller;

import com.google.zxing.WriterException;
import com.temple.entity.Booking;
import com.temple.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@Controller
public class BookingController {

    @GetMapping("/voicetest")
    public String voiceTest() {
        return "voicetest";
    }

    @Autowired
    private BookingService bookingService;

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/booking")
    public String bookingPage() {
        return "booking";
    }

    // ✅ CHANGED: gothram → raasi
    @PostMapping("/booking/submit")
    public String submitBooking(
            @RequestParam String name,
            @RequestParam String raasi,           // ✅ was: gothram
            @RequestParam String nakshatram,
            @RequestParam String phone,
            @RequestParam(required = false) String email,
            @RequestParam String archanaType,
            @RequestParam String timeSlot,
            Model model) {

        model.addAttribute("name",        name);
        model.addAttribute("raasi",       raasi);  // ✅ was: gothram
        model.addAttribute("nakshatram",  nakshatram);
        model.addAttribute("phone",       phone);
        model.addAttribute("email",       email);
        model.addAttribute("archanaType", archanaType);
        model.addAttribute("timeSlot",    timeSlot);
        model.addAttribute("amount",      bookingService.getArchanaPrice(archanaType));
        return "payment";
    }

    // ✅ CHANGED: gothram → raasi
    @PostMapping("/booking/confirm")
    public String confirmPayment(
            @RequestParam String name,
            @RequestParam String raasi,           // ✅ was: gothram
            @RequestParam String nakshatram,
            @RequestParam String phone,
            @RequestParam(required = false) String email,
            @RequestParam String archanaType,
            @RequestParam String timeSlot,
            Model model) {
        try {
            Booking booking = bookingService.createBooking(
                name, raasi, nakshatram, phone, email, archanaType, timeSlot
                // ✅ passing raasi where gothram was
            );
            model.addAttribute("booking", booking);
            model.addAttribute("devotee", booking.getDevotee());
            return "ticket";
        } catch (WriterException | IOException e) {
            model.addAttribute("error", "Booking failed: " + e.getMessage());
            return "booking";
        }
    }

    @GetMapping("/ticket/{bookingId}")
    public String viewTicket(@PathVariable String bookingId, Model model) {
        bookingService.findByBookingId(bookingId).ifPresent(booking -> {
            model.addAttribute("booking",  booking);
            model.addAttribute("devotee",  booking.getDevotee());
        });
        return "ticket";
    }

    @GetMapping("/audiotest")
    public String audioTestPage() {
        return "audiotest";
    }
}