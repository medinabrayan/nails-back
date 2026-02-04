package com.nails.catalog.repository;

import com.nails.catalog.entity.PortfolioImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, Long> {
    List<PortfolioImage> findBySpecialistId(Long specialistId);
}
