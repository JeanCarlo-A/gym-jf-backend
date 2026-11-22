package com.gymjf.backend.modules.routines.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateRoutineExerciseRequest {

    @Positive(message = "Las series debe ser mayor que cero")
    private Integer sets;

    @Positive(message = "Las repeticiones debe ser mayor que cero")
    private Integer reps;

    @PositiveOrZero(message = "El descanso no puede ser negativo")
    private Integer restSeconds;

}
