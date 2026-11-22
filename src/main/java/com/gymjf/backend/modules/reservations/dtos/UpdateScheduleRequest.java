package com.gymjf.backend.modules.reservations.dtos;

import java.time.LocalTime;

import com.gymjf.backend.modules.reservations.domain.WeekDay;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateScheduleRequest {
    @Size(min = 1, max = 45, message = "La actividad no puede estar vacío ni superar los 45 caracteres")
    private String activity;

    private WeekDay dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    @Positive(message = "El aforo máximo debe ser mayor que cero")
    private Integer maxCapacity;
}
