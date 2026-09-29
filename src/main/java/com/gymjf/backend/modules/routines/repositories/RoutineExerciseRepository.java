package com.gymjf.backend.modules.routines.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.routines.domain.RoutineExercise;

public interface RoutineExerciseRepository extends JpaRepository<RoutineExercise, Integer> {
    List<RoutineExercise> findByRoutineId(Integer routineId);
}
