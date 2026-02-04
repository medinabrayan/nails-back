package com.nails.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRequest {
    private String title;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
    private Long specialistId;
}
