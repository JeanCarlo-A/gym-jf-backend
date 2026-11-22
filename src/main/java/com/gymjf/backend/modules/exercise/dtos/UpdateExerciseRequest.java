package com.gymjf.backend.modules.exercise.dtos;

import com.gymjf.backend.modules.exercise.domain.MuscleGroup;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateExerciseRequest {

    @Size(min = 1, max = 45, message = "El nombre no puede estar vacío ni superar los 45 caracteres")
    private String name;

    @Size(min = 1, max = 255, message = "La descripción no puede estar vacío ni superar los 255 caracteres")
    private String description;

    private MuscleGroup muscleGroup;

    @Size(max = 255, message = "La URL del video no puede superar los 255 caracteres")
    private String videoUrl;

    private Boolean isForHome;

}
