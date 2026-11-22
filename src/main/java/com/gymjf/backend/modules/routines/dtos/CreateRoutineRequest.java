package com.gymjf.backend.modules.routines.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateRoutineRequest {

    @NotBlank(message = "El nombre de la rutina es obligatorio")
    @Size(max = 255, message = "El nombre de la rutina no puede superar los 255 caracteres")
    private String routineName;

    @NotNull(message = "El campo hecha por admin es obligatorio")
    private Boolean createdByAdmin;

    @NotNull(message = "El usuario es obligatorio")
    private Integer userId;

}
