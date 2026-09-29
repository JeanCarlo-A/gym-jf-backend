package com.gymjf.backend.modules.reservations.dtos;

import java.time.LocalDate;

import com.gymjf.backend.modules.reservations.domain.ReservationStatus;

import lombok.Data;

@Data
public class UpdateReservationRequest {
    private LocalDate reservationDate;

    private ReservationStatus status;
}
