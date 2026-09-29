package com.gymjf.backend.modules.reservations.dtos;

import java.time.LocalTime;

import com.gymjf.backend.modules.reservations.domain.WeekDay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private Integer id;
    private String activity;
    private WeekDay dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxCapacity;
}
