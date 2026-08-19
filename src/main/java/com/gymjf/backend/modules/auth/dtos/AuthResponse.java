package com.gymjf.backend.modules.auth.dtos;

import com.gymjf.backend.modules.auth.domain.Rol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private Integer id;
    private String name;
    private String email;
    private Rol rol;
}
