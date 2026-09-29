package com.gymjf.backend.modules.routines.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.modules.routines.domain.Routine;
import com.gymjf.backend.modules.routines.dtos.CreateRoutineRequest;
import com.gymjf.backend.modules.routines.dtos.RoutineResponse;
import com.gymjf.backend.modules.routines.dtos.UpdateRoutineRequest;
import com.gymjf.backend.modules.routines.repositories.RoutineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final UserRepository userRepository;

    public List<RoutineResponse> getAll() {
        return routineRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RoutineResponse> getByUserId(Integer userId) {
        return routineRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public RoutineResponse getById(Integer id) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada"));
        return mapToResponse(routine);
    }

    public RoutineResponse create(CreateRoutineRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Routine routine = Routine.builder()
                .routineName(request.getRoutineName())
                .createdByAdmin(request.getCreatedByAdmin())
                .user(user)
                .build();

        routineRepository.save(routine);

        return mapToResponse(routine);
    }

    public RoutineResponse update(Integer id, UpdateRoutineRequest request) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada"));

        if (request.getRoutineName() != null) {
            routine.setRoutineName(request.getRoutineName());
        }

        if (request.getCreatedByAdmin() != null) {
            routine.setCreatedByAdmin(request.getCreatedByAdmin());
        }

        routineRepository.save(routine);

        return mapToResponse(routine);
    }

    public void delete(Integer id) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada"));
        routineRepository.delete(routine);
    }

    private RoutineResponse mapToResponse(Routine routine) {
        return RoutineResponse.builder()
                .id(routine.getId())
                .routineName(routine.getRoutineName())
                .createdByAdmin(routine.isCreatedByAdmin())
                .userId(routine.getUser().getId())
                .build();
    }
}
