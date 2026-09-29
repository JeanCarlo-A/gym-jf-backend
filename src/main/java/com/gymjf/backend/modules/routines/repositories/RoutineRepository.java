package com.gymjf.backend.modules.routines.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.routines.domain.Routine;

public interface RoutineRepository extends JpaRepository<Routine, Integer> {
    List<Routine> findByUserId(Integer userId);
}
