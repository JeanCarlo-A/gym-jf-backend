package com.gymjf.backend.modules.routines.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gymjf.backend.modules.exercise.domain.Exercise;
import com.gymjf.backend.modules.exercise.repositories.ExerciseRepository;
import com.gymjf.backend.modules.routines.domain.Routine;
import com.gymjf.backend.modules.routines.domain.RoutineExercise;
import com.gymjf.backend.modules.routines.dtos.CreateRoutineExerciseRequest;
import com.gymjf.backend.modules.routines.dtos.RoutineExerciseResponse;
import com.gymjf.backend.modules.routines.dtos.UpdateRoutineExerciseRequest;
import com.gymjf.backend.modules.routines.repositories.RoutineExerciseRepository;
import com.gymjf.backend.modules.routines.repositories.RoutineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoutineExerciseService {

    private final RoutineExerciseRepository routineExerciseRepository;
    private final RoutineRepository routineRepository;
    private final ExerciseRepository exerciseRepository;

    public List<RoutineExerciseResponse> getAll() {
        return routineExerciseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RoutineExerciseResponse> getByRoutineId(Integer routineId) {
        return routineExerciseRepository.findByRoutineId(routineId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public RoutineExerciseResponse getById(Integer id) {
        RoutineExercise routineExercise = routineExerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ejercicio de rutina no encontrado"));
        return mapToResponse(routineExercise);
    }

    public RoutineExerciseResponse create(CreateRoutineExerciseRequest request) {
        Routine routine = routineRepository.findById(request.getRoutineId())
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada"));

        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado"));

        RoutineExercise routineExercise = RoutineExercise.builder()
                .sets(request.getSets())
                .reps(request.getReps())
                .restSeconds(request.getRestSeconds())
                .routine(routine)
                .exercise(exercise)
                .build();

        routineExerciseRepository.save(routineExercise);

        return mapToResponse(routineExercise);
    }

    public RoutineExerciseResponse update(Integer id, UpdateRoutineExerciseRequest request) {
        RoutineExercise routineExercise = routineExerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ejercicio de rutina no encontrado"));

        if (request.getSets() != null) {
            routineExercise.setSets(request.getSets());
        }

        if (request.getReps() != null) {
            routineExercise.setReps(request.getReps());
        }

        if (request.getRestSeconds() != null) {
            routineExercise.setRestSeconds(request.getRestSeconds());
        }

        routineExerciseRepository.save(routineExercise);

        return mapToResponse(routineExercise);
    }

    public void delete(Integer id) {
        RoutineExercise routineExercise = routineExerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ejercicio de rutina no encontrado"));
        routineExerciseRepository.delete(routineExercise);
    }

    private RoutineExerciseResponse mapToResponse(RoutineExercise routineExercise) {
        return RoutineExerciseResponse.builder()
                .id(routineExercise.getId())
                .sets(routineExercise.getSets())
                .reps(routineExercise.getReps())
                .restSeconds(routineExercise.getRestSeconds())
                .routineId(routineExercise.getRoutine().getId())
                .exerciseId(routineExercise.getExercise().getId())
                .build();
    }
}
