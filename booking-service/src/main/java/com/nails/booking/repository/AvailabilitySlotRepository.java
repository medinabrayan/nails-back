package com.nails.booking.repository;

import com.nails.booking.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {
    List<AvailabilitySlot> findBySpecialistId(Long specialistId);
}
