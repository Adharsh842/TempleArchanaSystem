package com.temple.repository;

import com.temple.entity.Devotee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DevoteeRepository extends JpaRepository<Devotee, Long> {
    Optional<Devotee> findByPhone(String phone);
}