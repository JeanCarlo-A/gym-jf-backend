package com.gymjf.backend.modules.routines.dtos;

import lombok.Data;

@Data
public class UpdateRoutineExerciseRequest {

    private Integer sets;

    private Integer reps;

    private Integer restSeconds;

}
