package com.gymjf.backend.modules.biometrics.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.biometrics.domain.AnthropometricRecord;

public interface AnthropometricRecordRepository extends JpaRepository<AnthropometricRecord, Integer> {
    Page<AnthropometricRecord> findByUserId(Integer userId, Pageable pageable);

    List<AnthropometricRecord> findByUserIdOrderByRecordDateDesc(Integer userId);
}
