package com.gymjf.backend.modules.reservations.dtos;

import java.time.LocalTime;

import com.gymjf.backend.modules.reservations.domain.WeekDay;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateScheduleRequest {
    @NotBlank(message = "La actividad es obligatoria")
    private String activity;

    @NotNull(message = "El día de la semana es obligatorio")
    private WeekDay dayOfWeek;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;

    @NotNull(message = "El aforo máximo es obligatorio")
    @Positive(message = "El aforo máximo debe ser positivo")
    private Integer maxCapacity;
}
