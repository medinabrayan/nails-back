package com.nails.booking.repository;

import com.nails.booking.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long>, RevisionRepository<Appointment, Long, Integer> {

    @Query("SELECT a FROM Appointment a WHERE a.specialistId = :specialistId " +
            "AND a.status != 'CANCELLED' " +
            "AND a.status != 'REJECTED' " +
            "AND ((a.scheduledAt < :endTime AND a.endTime > :startTime))")
    List<Appointment> findConflictingAppointments(Long specialistId, LocalDateTime startTime, LocalDateTime endTime);

    List<Appointment> findByClientId(Long clientId);

    List<Appointment> findBySpecialistId(Long specialistId);
}
