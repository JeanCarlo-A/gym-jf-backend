package com.gymjf.backend.modules.biometrics.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.modules.biometrics.domain.AnthropometricRecord;
import com.gymjf.backend.modules.biometrics.dtos.AnthropometricRecordResponse;
import com.gymjf.backend.modules.biometrics.dtos.CreateAnthropometricRecordRequest;
import com.gymjf.backend.modules.biometrics.dtos.UpdateAnthropometricRecordRequest;
import com.gymjf.backend.modules.biometrics.repositories.AnthropometricRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnthropometricRecordService {

    private final AnthropometricRecordRepository anthropometricRecordRepository;
    private final UserRepository userRepository;

    public List<AnthropometricRecordResponse> getAll() {
        return anthropometricRecordRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AnthropometricRecordResponse> getByUserId(Integer userId) {
        return anthropometricRecordRepository.findByUserIdOrderByRecordDateDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AnthropometricRecordResponse getById(Integer id) {
        AnthropometricRecord record = anthropometricRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ficha antropométrica no encontrada"));

        return mapToResponse(record);
    }

    public AnthropometricRecordResponse create(CreateAnthropometricRecordRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

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

    public AnthropometricRecordResponse update(Integer id, UpdateAnthropometricRecordRequest request) {
        AnthropometricRecord record = anthropometricRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ficha antropométrica no encontrada"));

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

    public void delete(Integer id) {
        anthropometricRecordRepository.deleteById(id);
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
