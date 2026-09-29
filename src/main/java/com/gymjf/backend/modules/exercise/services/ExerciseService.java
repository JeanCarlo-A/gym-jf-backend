package com.gymjf.backend.modules.exercise.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gymjf.backend.modules.exercise.domain.Exercise;
import com.gymjf.backend.modules.exercise.dtos.CreateExerciseRequest;
import com.gymjf.backend.modules.exercise.dtos.ExerciseResponse;
import com.gymjf.backend.modules.exercise.dtos.UpdateExerciseRequest;
import com.gymjf.backend.modules.exercise.repositories.ExerciseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    public List<ExerciseResponse> getAll() {
        return exerciseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ExerciseResponse getById(Integer id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado"));
        return mapToResponse(exercise);
    }

    public ExerciseResponse create(CreateExerciseRequest request) {
        Exercise exercise = Exercise.builder()
                .name(request.getName())
                .description(request.getDescription())
                .muscleGroup(request.getMuscleGroup())
                .videoUrl(request.getVideoUrl())
                .isForHome(request.getIsForHome())
                .build();

        exerciseRepository.save(exercise);

        return mapToResponse(exercise);
    }

    public ExerciseResponse update(Integer id, UpdateExerciseRequest request) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado"));

        if (request.getName() != null) {
            exercise.setName(request.getName());
        }

        if (request.getDescription() != null) {
            exercise.setDescription(request.getDescription());
        }

        if (request.getMuscleGroup() != null) {
            exercise.setMuscleGroup(request.getMuscleGroup());
        }

        if (request.getVideoUrl() != null) {
            exercise.setVideoUrl(request.getVideoUrl());
        }

        if (request.getIsForHome() != null) {
            exercise.setForHome(request.getIsForHome());
        }

        exerciseRepository.save(exercise);

        return mapToResponse(exercise);
    }

    public void delete(Integer id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado"));
        exerciseRepository.delete(exercise);
    }

    private ExerciseResponse mapToResponse(Exercise exercise) {
        return ExerciseResponse.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .description(exercise.getDescription())
                .muscleGroup(exercise.getMuscleGroup())
                .videoUrl(exercise.getVideoUrl())
                .isForHome(exercise.isForHome())
                .build();
    }
}
