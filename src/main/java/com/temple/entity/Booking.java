package com.temple.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", unique = true, nullable = false, length = 50)
    private String bookingId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "devotee_id", nullable = false)
    private Devotee devotee;

    // ✅ Snapshot fields — store at booking time so edits to Devotee don't affect old bookings
    @Column(name = "devotee_name", length = 100)
    private String devoteeName;

    @Column(name = "devotee_raasi", length = 100)
    private String devoteeRaasi;

    @Column(name = "devotee_nakshatram", length = 100)
    private String devoteeNakshatram;

    @Column(name = "archana_type", nullable = false, length = 100)
    private String archanaType;

    @Column(name = "time_slot", nullable = false, length = 50)
    private String timeSlot;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus = "PENDING";

    @Column(name = "booking_status", length = 20)
    private String bookingStatus = "CONFIRMED";

    @Column(name = "qr_code_path", length = 255)
    private String qrCodePath;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        bookingDate = LocalDate.now();
    }
}