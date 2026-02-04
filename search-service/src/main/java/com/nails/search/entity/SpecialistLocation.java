package com.nails.search.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "specialist_locations")
public class SpecialistLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long specialistId;

    private String specialistName;

    // Denormalized data
    private BigDecimal minPrice;
    private Double avgRating;
    private Boolean isAvailable;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;
}
