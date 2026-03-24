package com.temple.controller;

import com.temple.entity.Admin;
import com.temple.entity.Booking;
import com.temple.service.AdminService;
import com.temple.service.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private BookingService bookingService;

    // ── GET: Admin Login Page ────────────────────────────────────
    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    // ── POST: Admin Login Submit ─────────────────────────────────
    @PostMapping("/login")
    public String loginSubmit(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Optional<Admin> admin = adminService.login(username, password);
        if (admin.isPresent()) {
            session.setAttribute("loggedAdmin", admin.get().getName());
            return "redirect:/admin/dashboard";
        } else {
            model.addAttribute("error", "Invalid username or password!");
            return "admin/login";
        }
    }

    // ── GET: Admin Dashboard ─────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("loggedAdmin") == null) {
            return "redirect:/admin/login";
        }

        List<Booking> bookings = bookingService.getAllBookings();
        long todayCount = bookingService.getTodayBookingCount();
        long totalCount = bookings.size();
        long verifiedCount = bookings.stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsVerified())).count();

        model.addAttribute("bookings", bookings);
        model.addAttribute("todayCount", todayCount);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("verifiedCount", verifiedCount);
        model.addAttribute("adminName", session.getAttribute("loggedAdmin"));

        return "admin/dashboard";
    }

    // ── GET: QR Scanner Page ─────────────────────────────────────
    @GetMapping("/scanner")
    public String scannerPage(HttpSession session) {
        if (session.getAttribute("loggedAdmin") == null) {
            return "redirect:/admin/login";
        }
        return "admin/scanner";
    }

    // ── GET: Admin Logout ────────────────────────────────────────
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }
}