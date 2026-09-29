package com.gymjf.backend.modules.biometrics.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gymjf.backend.modules.auth.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fichas_antropometricas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnthropometricRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user;

    @Column(name = "peso", nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "altura", nullable = false, precision = 3, scale = 2)
    private BigDecimal height;

    @Column(name = "porcentaje_grasa", precision = 4, scale = 2)
    private BigDecimal bodyFatPercentage;

    @Column(name = "hombros", precision = 4, scale = 1)
    private BigDecimal shoulders;

    @Column(name = "pecho", precision = 4, scale = 1)
    private BigDecimal chest;

    @Column(name = "cintura", precision = 4, scale = 1)
    private BigDecimal waist;

    @Column(name = "cadera", precision = 4, scale = 1)
    private BigDecimal hip;

    @Column(name = "brazo", precision = 4, scale = 1)
    private BigDecimal arm;

    @Column(name = "muslo", precision = 4, scale = 1)
    private BigDecimal thigh;

    @Column(name = "pantorrilla", precision = 4, scale = 1)
    private BigDecimal calf;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate recordDate;

}
