package com.nails.booking.controller;

import com.nails.booking.dto.BookingRequest;
import com.nails.booking.entity.Appointment;
import com.nails.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Appointment> createBooking(@RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.createAppointment(request));
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getClientBookings(@RequestParam("clientId") Long clientId) {
        return ResponseEntity.ok(bookingService.getAppointmentsByClient(clientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getAppointmentById(id));
    }
}
