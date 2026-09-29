package com.gymjf.backend.modules.exercise.dtos;

import com.gymjf.backend.modules.exercise.domain.MuscleGroup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseResponse {

    private Integer id;

    private String name;

    private String description;

    private MuscleGroup muscleGroup;

    private String videoUrl;

    private boolean isForHome;

}
