package com.gymjf.backend.modules.reservations.services;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.gymjf.backend.shared.domain.dtos.PageResponse;
import lombok.RequiredArgsConstructor;
import com.gymjf.backend.shared.infrastructure.security.CurrentUser;
import com.gymjf.backend.shared.domain.exceptions.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CurrentUser currentUser;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public PageResponse<ReservationResponse> getAll(Pageable pageable) {
        Page<Reservation> page = currentUser.isAdmin()
                ? reservationRepository.findAll(pageable)
                : reservationRepository.findByUserId(currentUser.id(), pageable);
        return PageResponse.from(page, this::mapToResponse);
    }

    public PageResponse<ReservationResponse> getByUserId(Integer userId, Pageable pageable) {
        currentUser.requireOwnerOrAdmin(userId);
        return PageResponse.from(reservationRepository.findByUserId(userId, pageable), this::mapToResponse);
    }

    public ReservationResponse getById(Integer id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
        currentUser.requireOwnerOrAdmin(reservation.getUser().getId());
        return mapToResponse(reservation);
    }

    @Transactional
    public ReservationResponse create(CreateReservationRequest request) {
        currentUser.requireOwnerOrAdmin(request.getUserId());
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado"));

        Reservation reservation = Reservation.builder()
                .user(user)
                .schedule(schedule)
                .reservationDate(request.getReservationDate())
                .status(currentUser.isAdmin() && request.getStatus() != null ? request.getStatus() : ReservationStatus.PENDING)
                .build();

        reservationRepository.save(reservation);

        return mapToResponse(reservation);
    }

    @Transactional
    public ReservationResponse update(Integer id, UpdateReservationRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        if (request.getReservationDate() != null) {
            reservation.setReservationDate(request.getReservationDate());
        }
        if (request.getStatus() != null) {
            reservation.setStatus(request.getStatus());
        }

        reservationRepository.save(reservation);

        return mapToResponse(reservation);
    }

    @Transactional
    public void delete(Integer id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
        currentUser.requireOwnerOrAdmin(reservation.getUser().getId());
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
