package com.nails.reviews.repository;

import com.nails.reviews.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByAppointmentId(Long appointmentId);

    List<Review> findBySpecialistId(Long specialistId);
}
