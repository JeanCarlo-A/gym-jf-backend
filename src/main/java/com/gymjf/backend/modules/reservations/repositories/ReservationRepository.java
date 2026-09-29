package com.gymjf.backend.modules.reservations.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.reservations.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByUserId(Integer userId);

    List<Reservation> findByScheduleId(Integer scheduleId);
}
