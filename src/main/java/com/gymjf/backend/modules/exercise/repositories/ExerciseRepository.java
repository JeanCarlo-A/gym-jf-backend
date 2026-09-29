package com.gymjf.backend.modules.exercise.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.exercise.domain.Exercise;

public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {

    Optional<Exercise> findByName(String name);

    Optional<Exercise> findById(Integer id);
}
