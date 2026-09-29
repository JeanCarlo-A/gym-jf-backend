package com.gymjf.backend.modules.routines.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineResponse {

    private Integer id;

    private String routineName;

    private Boolean createdByAdmin;

    private Integer userId;

}
