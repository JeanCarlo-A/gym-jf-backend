package com.gymjf.backend.modules.reservations.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.reservations.domain.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
}
