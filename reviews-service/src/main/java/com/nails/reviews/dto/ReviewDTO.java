package com.nails.reviews.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Long appointmentId;
    private Integer rating;
    private String comment;
    private Long specialistId;
    private LocalDateTime createdAt;
}
