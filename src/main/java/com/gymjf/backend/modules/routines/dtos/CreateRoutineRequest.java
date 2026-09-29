package com.gymjf.backend.modules.routines.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRoutineRequest {

    @NotBlank(message = "El nombre de la rutina es obligatorio")
    private String routineName;

    @NotNull(message = "El campo hecha por admin es obligatorio")
    private Boolean createdByAdmin;

    @NotNull(message = "El usuario es obligatorio")
    private Integer userId;

}
