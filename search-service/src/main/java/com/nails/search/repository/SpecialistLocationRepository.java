package com.nails.search.repository;

import com.nails.search.entity.SpecialistLocation;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SpecialistLocationRepository extends JpaRepository<SpecialistLocation, Long> {

    @Query(value = "SELECT * FROM specialist_locations s " +
            "WHERE ST_DistanceSphere(s.location, :userLocation) <= :radiusAndMeters " +
            "AND (:minPrice IS NULL OR s.min_price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR s.min_price <= :maxPrice) " +
            "AND (:minRating IS NULL OR s.avg_rating >= :minRating)", nativeQuery = true)
    List<SpecialistLocation> searchSpecialists(
            @Param("userLocation") Point userLocation,
            @Param("radiusAndMeters") double radiusAndMeters,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minRating") Double minRating);

    SpecialistLocation findBySpecialistId(Long specialistId);
}
