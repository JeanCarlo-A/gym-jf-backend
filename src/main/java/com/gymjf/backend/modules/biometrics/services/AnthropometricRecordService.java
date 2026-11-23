package com.gymjf.backend.modules.biometrics.services;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.modules.biometrics.domain.AnthropometricRecord;
import com.gymjf.backend.modules.biometrics.dtos.AnthropometricRecordResponse;
import com.gymjf.backend.modules.biometrics.dtos.CreateAnthropometricRecordRequest;
import com.gymjf.backend.modules.biometrics.dtos.UpdateAnthropometricRecordRequest;
import com.gymjf.backend.modules.biometrics.repositories.AnthropometricRecordRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.gymjf.backend.shared.domain.dtos.PageResponse;
import lombok.RequiredArgsConstructor;
import com.gymjf.backend.shared.infrastructure.security.CurrentUser;
import com.gymjf.backend.shared.domain.exceptions.ConflictException;
import com.gymjf.backend.shared.domain.exceptions.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnthropometricRecordService {

    private final AnthropometricRecordRepository anthropometricRecordRepository;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;

    public PageResponse<AnthropometricRecordResponse> getAll(Pageable pageable) {
        Page<AnthropometricRecord> page = currentUser.isAdmin()
                ? anthropometricRecordRepository.findAll(pageable)
                : anthropometricRecordRepository.findByUserId(currentUser.id(), pageable);
        return PageResponse.from(page, this::mapToResponse);
    }

    public PageResponse<AnthropometricRecordResponse> getByUserId(Integer userId, Pageable pageable) {
        currentUser.requireOwnerOrAdmin(userId);
        return PageResponse.from(anthropometricRecordRepository.findByUserId(userId, pageable), this::mapToResponse);
    }

    public AnthropometricRecordResponse getById(Integer id) {
        AnthropometricRecord record = anthropometricRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha antropométrica no encontrada"));
        currentUser.requireOwnerOrAdmin(record.getUser().getId());

        return mapToResponse(record);
    }

    @Transactional
    public AnthropometricRecordResponse create(CreateAnthropometricRecordRequest request) {
        currentUser.requireOwnerOrAdmin(request.getUserId());
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (user.getHealthConsentAt() == null) {
            throw new ConflictException("Antes de registrar medidas corporales debes aceptar el consentimiento informado "
                    + "sobre el uso de tus datos de salud (POST /api/v1/users/me/health-consent)");
        }

        AnthropometricRecord record = AnthropometricRecord.builder()
                .user(user)
                .weight(request.getWeight())
                .height(request.getHeight())
                .bodyFatPercentage(request.getBodyFatPercentage())
                .shoulders(request.getShoulders())
                .chest(request.getChest())
                .waist(request.getWaist())
                .hip(request.getHip())
                .arm(request.getArm())
                .thigh(request.getThigh())
                .calf(request.getCalf())
                .recordDate(request.getRecordDate())
                .build();

        anthropometricRecordRepository.save(record);

        return mapToResponse(record);
    }

    @Transactional
    public AnthropometricRecordResponse update(Integer id, UpdateAnthropometricRecordRequest request) {
        AnthropometricRecord record = anthropometricRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha antropométrica no encontrada"));
        currentUser.requireOwnerOrAdmin(record.getUser().getId());

        if (request.getWeight() != null) {
            record.setWeight(request.getWeight());
        }

        if (request.getHeight() != null) {
            record.setHeight(request.getHeight());
        }

        if (request.getBodyFatPercentage() != null) {
            record.setBodyFatPercentage(request.getBodyFatPercentage());
        }

        if (request.getShoulders() != null) {
            record.setShoulders(request.getShoulders());
        }

        if (request.getChest() != null) {
            record.setChest(request.getChest());
        }

        if (request.getWaist() != null) {
            record.setWaist(request.getWaist());
        }

        if (request.getHip() != null) {
            record.setHip(request.getHip());
        }

        if (request.getArm() != null) {
            record.setArm(request.getArm());
        }

        if (request.getThigh() != null) {
            record.setThigh(request.getThigh());
        }

        if (request.getCalf() != null) {
            record.setCalf(request.getCalf());
        }

        if (request.getRecordDate() != null) {
            record.setRecordDate(request.getRecordDate());
        }

        anthropometricRecordRepository.save(record);

        return mapToResponse(record);
    }

    @Transactional
    public void delete(Integer id) {
        AnthropometricRecord record = anthropometricRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha antropométrica no encontrada"));
        currentUser.requireOwnerOrAdmin(record.getUser().getId());
        anthropometricRecordRepository.delete(record);
    }

    private AnthropometricRecordResponse mapToResponse(AnthropometricRecord entity) {
        return AnthropometricRecordResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .weight(entity.getWeight())
                .height(entity.getHeight())
                .bodyFatPercentage(entity.getBodyFatPercentage())
                .shoulders(entity.getShoulders())
                .chest(entity.getChest())
                .waist(entity.getWaist())
                .hip(entity.getHip())
                .arm(entity.getArm())
                .thigh(entity.getThigh())
                .calf(entity.getCalf())
                .recordDate(entity.getRecordDate())
                .build();
    }
}
