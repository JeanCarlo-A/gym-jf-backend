package com.gymjf.backend.modules.exercise.dtos;

import com.gymjf.backend.modules.exercise.domain.MuscleGroup;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateExerciseRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 45, message = "El nombre no puede superar los 45 caracteres")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String description;

    @NotNull(message = "El grupo muscular es obligatorio")
    private MuscleGroup muscleGroup;

    @Size(max = 255, message = "La URL del video no puede superar los 255 caracteres")
    private String videoUrl;

    @NotNull(message = "Debe indicar si el ejercicio es para casa")
    private Boolean isForHome;

}
