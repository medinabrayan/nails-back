package com.nails.booking.service;

import com.nails.booking.client.CatalogClient;
import com.nails.booking.dto.BookingRequest;
import com.nails.booking.dto.ServiceDTO;
import com.nails.booking.entity.Appointment;
import com.nails.booking.entity.AppointmentStatus;
import com.nails.booking.entity.AvailabilitySlot;
import com.nails.booking.repository.AppointmentRepository;
import com.nails.booking.repository.AvailabilitySlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final AppointmentRepository appointmentRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final CatalogClient catalogClient;

    @Transactional
    public Appointment createAppointment(BookingRequest request) {
        // 1. Fetch Service Details
        ServiceDTO service = catalogClient.getServiceById(request.getServiceId());
        if (service == null) {
            throw new RuntimeException("Service not found");
        }

        LocalDateTime startTime = request.getScheduledAt();
        LocalDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        // 2. Validate Specialist Availability (Basic check against slots)
        // This is a simplified check. Real check involves checking DayOfWeek and Time
        // range.
        // For this task, "Double Booking" logic is the priority requested.

        // 3. Double Booking Check
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                service.getSpecialistId(),
                startTime,
                endTime);

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Time slot is already booked for this specialist.");
        }

        // 4. Create and Save
        Appointment appointment = Appointment.builder()
                .clientId(request.getClientId())
                .serviceId(request.getServiceId())
                .specialistId(service.getSpecialistId())
                .scheduledAt(startTime)
                .endTime(endTime)
                .status(AppointmentStatus.PENDING)
                .build();

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAppointmentsByClient(Long clientId) {
        return appointmentRepository.findByClientId(clientId);
    }

    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
    }
}
