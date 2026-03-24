package com.temple.repository;

import com.temple.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonationRepository
        extends JpaRepository<Donation, Long> {

    List<Donation> findAllByOrderByDonatedAtDesc();

    Optional<Donation> findByReceiptNumber(String receiptNumber);

    List<Donation> findByDonationType(String donationType);

    @Query("SELECT SUM(d.amount) FROM Donation d " +
           "WHERE d.status = 'COMPLETED'")
    BigDecimal getTotalDonations();

    @Query("SELECT COUNT(d) FROM Donation d " +
           "WHERE d.donatedAt >= :startDate")
    long countDonationsSince(LocalDateTime startDate);
}