package com.gymjf.backend.modules.reservations.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.reservations.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    Page<Reservation> findByUserId(Integer userId, Pageable pageable);

    List<Reservation> findByUserId(Integer userId);

    List<Reservation> findByScheduleId(Integer scheduleId);
}
