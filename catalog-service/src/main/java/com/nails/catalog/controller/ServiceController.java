package com.nails.catalog.controller;

import com.nails.catalog.dto.ServiceRequest;
import com.nails.catalog.entity.ServiceItem;
import com.nails.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceController {

    private final CatalogService catalogService;

    @PostMapping
    public ResponseEntity<ServiceItem> createService(@RequestBody ServiceRequest request) {
        return ResponseEntity.ok(catalogService.createService(request));
    }

    @GetMapping
    public ResponseEntity<List<ServiceItem>> getServices(
            @RequestParam("specialistId") Long specialistId) {
        return ResponseEntity.ok(catalogService.getServicesBySpecialist(specialistId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceItem> getServiceById(@PathVariable Long id) {
        return catalogService.getServiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        catalogService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
