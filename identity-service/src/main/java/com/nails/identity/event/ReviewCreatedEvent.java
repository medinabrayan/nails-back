package com.nails.identity.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreatedEvent implements Serializable {
    private Long specialistId;
    private Integer rating;
}
