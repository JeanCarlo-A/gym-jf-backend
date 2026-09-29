package com.gymjf.backend.modules.routines.dtos;

import lombok.Data;

@Data
public class UpdateRoutineRequest {

    private String routineName;

    private Boolean createdByAdmin;

}
