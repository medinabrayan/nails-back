package com.nails.reviews.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "booking-service") // service name in service registry or url usually
public interface BookingClient {

    @GetMapping("/bookings/{id}")
    AppointmentResponse getAppointment(@PathVariable("id") Long id);

    // Inner DTO to map response
    record AppointmentResponse(Long id, Long specialistId, String status) {
    }
}
