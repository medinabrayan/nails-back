package com.nails.search.service;

import com.nails.search.dto.PriceChangedEvent;
import com.nails.search.dto.SpecialistDTO;
import com.nails.search.entity.SpecialistLocation;
import com.nails.search.repository.SpecialistLocationRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SpecialistLocationRepository repository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public List<SpecialistDTO> searchSpecialists(
            double latitude,
            double longitude,
            double radiusKm,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating) {
        Point userLocation = geometryFactory.createPoint(new Coordinate(longitude, latitude)); // x=long, y=lat
        double radiusMeters = radiusKm * 1000;

        List<SpecialistLocation> results = repository.searchSpecialists(
                userLocation,
                radiusMeters,
                minPrice,
                maxPrice,
                minRating);

        return results.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private SpecialistDTO mapToDTO(SpecialistLocation entity) {
        return SpecialistDTO.builder()
                .specialistId(entity.getSpecialistId())
                .name(entity.getSpecialistName())
                .minPrice(entity.getMinPrice())
                .avgRating(entity.getAvgRating())
                .latitude(entity.getLocation().getY())
                .longitude(entity.getLocation().getX())
                .build();
    }

    // For testing/seeding purposes
    @Transactional
    public void addOrUpdateLocation(Long specialistId, double lat, double lon, String name, BigDecimal price,
            Double rating) {
        SpecialistLocation loc = repository.findBySpecialistId(specialistId);
        if (loc == null) {
            loc = new SpecialistLocation();
            loc.setSpecialistId(specialistId);
        }
        loc.setSpecialistName(name);
        loc.setMinPrice(price);
        loc.setAvgRating(rating);
        loc.setLocation(geometryFactory.createPoint(new Coordinate(lon, lat)));
        loc.setIsAvailable(true);
        repository.save(loc);
    }

    @Bean
    public Consumer<PriceChangedEvent> priceChangedConsumer() {
        return event -> {
            SpecialistLocation loc = repository.findBySpecialistId(event.getSpecialistId());
            if (loc != null) {
                loc.setMinPrice(event.getNewMinPrice());
                repository.save(loc);
            }
        };
    }
}
