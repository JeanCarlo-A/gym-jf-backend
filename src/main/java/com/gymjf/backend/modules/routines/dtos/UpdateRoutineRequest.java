package com.gymjf.backend.modules.routines.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateRoutineRequest {

    @Size(min = 1, max = 255, message = "El nombre de la rutina no puede estar vacío ni superar los 255 caracteres")
    private String routineName;

    private Boolean createdByAdmin;

}
