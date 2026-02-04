package com.nails.reviews.service;

import com.nails.reviews.client.BookingClient;
import com.nails.reviews.dto.ReviewDTO;
import com.nails.reviews.entity.Review;
import com.nails.reviews.mapper.ReviewMapper;
import com.nails.reviews.producer.ReviewEventProducer;
import com.nails.reviews.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final BookingClient bookingClient;
    private final ReviewEventProducer reviewEventProducer;

    @Transactional
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        // 1. Verify Appointment via Feign
        BookingClient.AppointmentResponse appointment;
        try {
            appointment = bookingClient.getAppointment(reviewDTO.getAppointmentId());
        } catch (Exception e) {
            throw new RuntimeException("Could not verify appointment " + reviewDTO.getAppointmentId());
        }

        if (!"COMPLETED".equalsIgnoreCase(appointment.status())) {
            throw new IllegalArgumentException("Appointment is not COMPLETED. Cannot review.");
        }

        // 2. Prepare Review Entity
        Review review = reviewMapper.toEntity(reviewDTO);
        review.setSpecialistId(appointment.specialistId());

        // 3. Save
        Review savedReview = reviewRepository.save(review);

        // 4. Publish Event
        reviewEventProducer.publishReviewCreated(savedReview.getSpecialistId(), savedReview.getRating());

        return reviewMapper.toDTO(savedReview);
    }
}
