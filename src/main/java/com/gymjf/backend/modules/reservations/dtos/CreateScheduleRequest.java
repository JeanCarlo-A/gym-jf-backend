package com.gymjf.backend.modules.reservations.dtos;

import java.time.LocalTime;

import com.gymjf.backend.modules.reservations.domain.WeekDay;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateScheduleRequest {
    @NotBlank(message = "La actividad es obligatoria")
    @Size(max = 45, message = "La actividad no puede superar los 45 caracteres")
    private String activity;

    @NotNull(message = "El día de la semana es obligatorio")
    private WeekDay dayOfWeek;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;

    @NotNull(message = "El aforo máximo es obligatorio")
    @Positive(message = "El aforo máximo debe ser mayor que cero")
    private Integer maxCapacity;
}
