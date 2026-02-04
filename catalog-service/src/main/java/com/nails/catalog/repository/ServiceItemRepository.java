package com.nails.catalog.repository;

import com.nails.catalog.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {
    List<ServiceItem> findBySpecialistId(Long specialistId);
}
