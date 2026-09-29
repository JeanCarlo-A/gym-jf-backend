package com.gymjf.backend.modules.reservations.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.modules.reservations.domain.Reservation;
import com.gymjf.backend.modules.reservations.domain.ReservationStatus;
import com.gymjf.backend.modules.reservations.domain.Schedule;
import com.gymjf.backend.modules.reservations.dtos.CreateReservationRequest;
import com.gymjf.backend.modules.reservations.dtos.ReservationResponse;
import com.gymjf.backend.modules.reservations.dtos.UpdateReservationRequest;
import com.gymjf.backend.modules.reservations.repositories.ReservationRepository;
import com.gymjf.backend.modules.reservations.repositories.ScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public List<ReservationResponse> getAll() {
        return reservationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReservationResponse> getByUserId(Integer userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ReservationResponse getById(Integer id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        return mapToResponse(reservation);
    }

    public ReservationResponse create(CreateReservationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        Reservation reservation = Reservation.builder()
                .user(user)
                .schedule(schedule)
                .reservationDate(request.getReservationDate())
                .status(request.getStatus() != null ? request.getStatus() : ReservationStatus.PENDING)
                .build();

        reservationRepository.save(reservation);

        return mapToResponse(reservation);
    }

    public ReservationResponse update(Integer id, UpdateReservationRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (request.getReservationDate() != null) {
            reservation.setReservationDate(request.getReservationDate());
        }
        if (request.getStatus() != null) {
            reservation.setStatus(request.getStatus());
        }

        reservationRepository.save(reservation);

        return mapToResponse(reservation);
    }

    public void delete(Integer id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        reservationRepository.delete(reservation);
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .scheduleId(reservation.getSchedule().getId())
                .reservationDate(reservation.getReservationDate())
                .status(reservation.getStatus())
                .build();
    }
}
