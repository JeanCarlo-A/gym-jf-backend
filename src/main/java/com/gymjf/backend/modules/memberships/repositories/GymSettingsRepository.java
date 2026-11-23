package com.gymjf.backend.modules.memberships.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.memberships.domain.GymSettings;

public interface GymSettingsRepository extends JpaRepository<GymSettings, Integer> {
}
