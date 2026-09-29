package com.gymjf.backend.modules.exercise.dtos;

import com.gymjf.backend.modules.exercise.domain.MuscleGroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateExerciseRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotNull(message = "El grupo muscular es obligatorio")
    private MuscleGroup muscleGroup;

    private String videoUrl;

    @NotNull(message = "Debe indicar si el ejercicio es para casa")
    private Boolean isForHome;

}
