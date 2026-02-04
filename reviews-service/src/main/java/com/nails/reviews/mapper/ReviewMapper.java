package com.nails.reviews.mapper;

import com.nails.reviews.dto.ReviewDTO;
import com.nails.reviews.entity.Review;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    ReviewDTO toDTO(Review review);

    Review toEntity(ReviewDTO reviewDTO);
}
