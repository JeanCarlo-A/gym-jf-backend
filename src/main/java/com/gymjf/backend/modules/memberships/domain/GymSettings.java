package com.gymjf.backend.modules.memberships.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Ajustes globales del gimnasio. Es una sola fila (id = 1). */
@Entity
@Table(name = "ajustes_gimnasio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymSettings {

    public static final int SINGLE_ID = 1;

    @Id
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_acceso", nullable = false, length = 20)
    private AccessMode accessMode;
}
