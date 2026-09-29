package com.gymjf.backend.modules.exercise.dtos;

import com.gymjf.backend.modules.exercise.domain.MuscleGroup;

import lombok.Data;

@Data
public class UpdateExerciseRequest {

    private String name;

    private String description;

    private MuscleGroup muscleGroup;

    private String videoUrl;

    private Boolean isForHome;

}
