package com.gymjf.backend.modules.routines.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateRoutineExerciseRequest {

    @NotNull(message = "Las series son obligatorias")
    @Positive(message = "Las series deben ser un número positivo")
    private Integer sets;

    @NotNull(message = "Las repeticiones son obligatorias")
    @Positive(message = "Las repeticiones deben ser un número positivo")
    private Integer reps;

    @NotNull(message = "El descanso es obligatorio")
    private Integer restSeconds;

    @NotNull(message = "La rutina es obligatoria")
    private Integer routineId;

    @NotNull(message = "El ejercicio es obligatorio")
    private Integer exerciseId;

}
