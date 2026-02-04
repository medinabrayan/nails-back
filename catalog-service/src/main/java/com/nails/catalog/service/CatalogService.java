package com.nails.catalog.service;

import com.nails.catalog.client.IdentityClient;
import com.nails.catalog.dto.PortfolioRequest;
import com.nails.catalog.dto.ServiceRequest;
import com.nails.catalog.entity.PortfolioImage;
import com.nails.catalog.entity.ServiceItem;
import com.nails.catalog.repository.PortfolioImageRepository;
import com.nails.catalog.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ServiceItemRepository serviceItemRepository;
    private final PortfolioImageRepository portfolioImageRepository;
    private final IdentityClient identityClient;

    @Transactional
    public ServiceItem createService(ServiceRequest request) {
        validateSpecialist(request.getSpecialistId());

        ServiceItem serviceItem = ServiceItem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationMinutes(request.getDurationMinutes())
                .specialistId(request.getSpecialistId())
                .build();

        return serviceItemRepository.save(serviceItem);
    }

    public List<ServiceItem> getServicesBySpecialist(Long specialistId) {
        // validateSpecialist(specialistId); // Optional: if we want to ensure
        // specialist exists before querying
        return serviceItemRepository.findBySpecialistId(specialistId);
    }

    public java.util.Optional<ServiceItem> getServiceById(Long id) {
        return serviceItemRepository.findById(id);
    }

    public void deleteService(Long id) {
        serviceItemRepository.deleteById(id);
    }

    @Transactional
    public PortfolioImage addPortfolioImage(PortfolioRequest request) {
        validateSpecialist(request.getSpecialistId());

        PortfolioImage image = PortfolioImage.builder()
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .specialistId(request.getSpecialistId())
                .build();

        return portfolioImageRepository.save(image);
    }

    public List<PortfolioImage> getPortfolioBySpecialist(Long specialistId) {
        return portfolioImageRepository.findBySpecialistId(specialistId);
    }

    private void validateSpecialist(Long specialistId) {
        try {
            boolean exists = identityClient.checkUserExists(specialistId);
            if (!exists) {
                throw new RuntimeException("Specialist with ID " + specialistId + " not found in Identity Service");
            }
        } catch (Exception e) {
            // Fallback or rethrow.
            // In a real scenario, we might want to handle FeignException explicitly.
            // For now, if identity service is down or returns 404, we treat it as
            // validation failure or system error.
            throw new RuntimeException("Could not validate specialist: " + e.getMessage());
        }
    }
}
