package com.gymjf.backend.modules.reservations.dtos;

import java.time.LocalTime;

import com.gymjf.backend.modules.reservations.domain.WeekDay;

import lombok.Data;

@Data
public class UpdateScheduleRequest {
    private String activity;

    private WeekDay dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer maxCapacity;
}
