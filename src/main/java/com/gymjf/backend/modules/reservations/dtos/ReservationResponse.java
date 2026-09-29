package com.gymjf.backend.modules.reservations.dtos;

import java.time.LocalDate;

import com.gymjf.backend.modules.reservations.domain.ReservationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private Integer id;
    private Integer userId;
    private Integer scheduleId;
    private LocalDate reservationDate;
    private ReservationStatus status;
}
