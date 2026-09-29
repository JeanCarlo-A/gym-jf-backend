package com.gymjf.backend.modules.routines.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineExerciseResponse {

    private Integer id;

    private Integer sets;

    private Integer reps;

    private Integer restSeconds;

    private Integer routineId;

    private Integer exerciseId;

}
