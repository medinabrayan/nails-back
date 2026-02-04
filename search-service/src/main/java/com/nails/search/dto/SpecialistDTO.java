package com.nails.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialistDTO {
    private Long specialistId;
    private String name;
    private BigDecimal minPrice;
    private Double avgRating;
    private Double distance; // Calculated if needed, or just return basic info
    private Double latitude;
    private Double longitude;
}
