package com.nails.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceChangedEvent {
    private Long specialistId;
    private BigDecimal newMinPrice;
    // Add other fields as necessary
}
