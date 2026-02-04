package com.nails.search.controller;

import com.nails.search.dto.SpecialistDTO;
import com.nails.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<List<SpecialistDTO>> search(
            @RequestParam("lat") double latitude,
            @RequestParam("lon") double longitude,
            @RequestParam(value = "radius", defaultValue = "10") double radiusKm,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "minRating", required = false) Double minRating) {
        return ResponseEntity.ok(searchService.searchSpecialists(
                latitude, longitude, radiusKm, minPrice, maxPrice, minRating));
    }

    // Helper to seed data manually if needed (since we don't have full event
    // pipeline from Catalog yet)
    @PostMapping("/seed")
    public ResponseEntity<Void> seedData(
            @RequestParam Long specialistId,
            @RequestParam String name,
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam BigDecimal price,
            @RequestParam double rating) {
        searchService.addOrUpdateLocation(specialistId, lat, lon, name, price, rating);
        return ResponseEntity.ok().build();
    }
}
