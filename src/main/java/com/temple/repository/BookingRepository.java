package com.temple.repository;

import com.temple.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingId(String bookingId);

    List<Booking> findAllByOrderByCreatedAtDesc();

    List<Booking> findByBookingDate(LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.isVerified = false ORDER BY b.createdAt DESC")
    List<Booking> findUnverifiedBookings();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingDate = :date")
    long countByBookingDate(LocalDate date);

    List<Booking> findByPaymentStatus(String status);
}