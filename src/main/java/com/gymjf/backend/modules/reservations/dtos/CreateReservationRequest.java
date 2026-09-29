package com.gymjf.backend.modules.reservations.dtos;

import java.time.LocalDate;

import com.gymjf.backend.modules.reservations.domain.ReservationStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReservationRequest {
    @NotNull(message = "El usuario es obligatorio")
    private Integer userId;

    @NotNull(message = "El horario es obligatorio")
    private Integer scheduleId;

    @NotNull(message = "La fecha de reserva es obligatoria")
    private LocalDate reservationDate;

    private ReservationStatus status;
}
