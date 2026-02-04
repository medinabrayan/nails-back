package com.nails.catalog.controller;

import com.nails.catalog.dto.PortfolioRequest;
import com.nails.catalog.entity.PortfolioImage;
import com.nails.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final CatalogService catalogService;

    @PostMapping
    public ResponseEntity<PortfolioImage> addImage(@RequestBody PortfolioRequest request) {
        return ResponseEntity.ok(catalogService.addPortfolioImage(request));
    }

    @GetMapping
    public ResponseEntity<List<PortfolioImage>> getPortfolio(
            @RequestParam("specialistId") Long specialistId) {
        return ResponseEntity.ok(catalogService.getPortfolioBySpecialist(specialistId));
    }
}
