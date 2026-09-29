package com.gymjf.backend.modules.reservations.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gymjf.backend.modules.reservations.domain.Schedule;
import com.gymjf.backend.modules.reservations.dtos.CreateScheduleRequest;
import com.gymjf.backend.modules.reservations.dtos.ScheduleResponse;
import com.gymjf.backend.modules.reservations.dtos.UpdateScheduleRequest;
import com.gymjf.backend.modules.reservations.repositories.ScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public List<ScheduleResponse> getAll() {
        return scheduleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ScheduleResponse getById(Integer id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));
        return mapToResponse(schedule);
    }

    public ScheduleResponse create(CreateScheduleRequest request) {
        Schedule schedule = Schedule.builder()
                .activity(request.getActivity())
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .maxCapacity(request.getMaxCapacity())
                .build();

        scheduleRepository.save(schedule);

        return mapToResponse(schedule);
    }

    public ScheduleResponse update(Integer id, UpdateScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        if (request.getActivity() != null) {
            schedule.setActivity(request.getActivity());
        }
        if (request.getDayOfWeek() != null) {
            schedule.setDayOfWeek(request.getDayOfWeek());
        }
        if (request.getStartTime() != null) {
            schedule.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            schedule.setEndTime(request.getEndTime());
        }
        if (request.getMaxCapacity() != null) {
            schedule.setMaxCapacity(request.getMaxCapacity());
        }

        scheduleRepository.save(schedule);

        return mapToResponse(schedule);
    }

    public void delete(Integer id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));
        scheduleRepository.delete(schedule);
    }

    private ScheduleResponse mapToResponse(Schedule schedule) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .activity(schedule.getActivity())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .maxCapacity(schedule.getMaxCapacity())
                .build();
    }
}
