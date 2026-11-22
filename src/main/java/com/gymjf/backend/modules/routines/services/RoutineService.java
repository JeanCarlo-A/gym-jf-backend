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
import com.gymjf.backend.shared.infrastructure.security.CurrentUser;
import com.gymjf.backend.shared.domain.exceptions.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;

    public List<RoutineResponse> getAll() {
        return (currentUser.isAdmin() ? routineRepository.findAll() : routineRepository.findByUserId(currentUser.id())).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RoutineResponse> getByUserId(Integer userId) {
        currentUser.requireOwnerOrAdmin(userId);
        return routineRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public RoutineResponse getById(Integer id) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));
        currentUser.requireOwnerOrAdmin(routine.getUser().getId());
        return mapToResponse(routine);
    }

    public RoutineResponse create(CreateRoutineRequest request) {
        currentUser.requireOwnerOrAdmin(request.getUserId());
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Routine routine = Routine.builder()
                .routineName(request.getRoutineName())
                .createdByAdmin(currentUser.isAdmin() && Boolean.TRUE.equals(request.getCreatedByAdmin()))
                .user(user)
                .build();

        routineRepository.save(routine);

        return mapToResponse(routine);
    }

    public RoutineResponse update(Integer id, UpdateRoutineRequest request) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));
        currentUser.requireOwnerOrAdmin(routine.getUser().getId());

        if (request.getRoutineName() != null) {
            routine.setRoutineName(request.getRoutineName());
        }

        if (request.getCreatedByAdmin() != null && currentUser.isAdmin()) {
            routine.setCreatedByAdmin(request.getCreatedByAdmin());
        }

        routineRepository.save(routine);

        return mapToResponse(routine);
    }

    public void delete(Integer id) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));
        currentUser.requireOwnerOrAdmin(routine.getUser().getId());
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
