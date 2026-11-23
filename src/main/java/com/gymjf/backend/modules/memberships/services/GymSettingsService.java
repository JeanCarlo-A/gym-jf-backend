package com.gymjf.backend.modules.memberships.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gymjf.backend.modules.memberships.domain.AccessMode;
import com.gymjf.backend.modules.memberships.domain.GymSettings;
import com.gymjf.backend.modules.memberships.dtos.GymSettingsResponse;
import com.gymjf.backend.modules.memberships.dtos.UpdateGymSettingsRequest;
import com.gymjf.backend.modules.memberships.repositories.GymSettingsRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GymSettingsService {

    private final GymSettingsRepository gymSettingsRepository;

    public GymSettingsResponse get() {
        return toResponse(load());
    }

    public AccessMode currentMode() {
        return load().getAccessMode();
    }

    @Transactional
    public GymSettingsResponse update(UpdateGymSettingsRequest request) {
        GymSettings settings = load();
        settings.setAccessMode(request.getAccessMode());
        return toResponse(gymSettingsRepository.save(settings));
    }

    /** Si la fila única no existe (BD recién migrada a mano), se crea en modo FREE. */
    private GymSettings load() {
        return gymSettingsRepository.findById(GymSettings.SINGLE_ID)
                .orElseGet(() -> gymSettingsRepository.save(
                        GymSettings.builder().id(GymSettings.SINGLE_ID).accessMode(AccessMode.FREE).build()));
    }

    private GymSettingsResponse toResponse(GymSettings settings) {
        return GymSettingsResponse.builder().accessMode(settings.getAccessMode()).build();
    }
}
