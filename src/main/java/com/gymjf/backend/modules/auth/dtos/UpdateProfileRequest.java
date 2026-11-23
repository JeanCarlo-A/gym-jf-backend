package com.gymjf.backend.modules.auth.dtos;

import java.time.LocalDate;

import com.gymjf.backend.modules.auth.domain.PhysicalGoal;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Datos que el propio usuario puede cambiar de su perfil. Los campos que no se envían no se tocan. */
@Data
public class UpdateProfileRequest {

    @Size(min = 1, max = 150, message = "El nombre debe tener entre 1 y 150 caracteres")
    private String name;

    @Past(message = "La fecha de nacimiento debe estar en el pasado")
    private LocalDate birthDate;

    @Size(max = 255, message = "La URL de la foto no puede superar los 255 caracteres")
    private String profilePictureUrl;

    private PhysicalGoal physicalGoal;
}
