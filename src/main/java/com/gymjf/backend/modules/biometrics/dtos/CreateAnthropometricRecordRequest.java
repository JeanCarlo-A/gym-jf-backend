package com.gymjf.backend.modules.biometrics.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateAnthropometricRecordRequest {
    @NotNull(message = "El usuario es obligatorio")
    private Integer userId;

    @NotNull(message = "El peso es obligatorio")
    @Positive(message = "El peso debe ser mayor que cero")
    @Digits(integer = 3, fraction = 2, message = "Valor no válido para el peso: máximo 3 dígito(s) entero(s) y 2 decimal(es)")
    private BigDecimal weight;

    @NotNull(message = "La altura es obligatoria")
    @Positive(message = "La altura debe ser mayor que cero")
    @Digits(integer = 1, fraction = 2, message = "Valor no válido para la altura (en metros, p. ej. 1.75): máximo 1 dígito(s) entero(s) y 2 decimal(es)")
    private BigDecimal height;

    @PositiveOrZero(message = "El porcentaje de grasa no puede ser negativo")
    @Digits(integer = 2, fraction = 2, message = "Valor no válido para el porcentaje de grasa: máximo 2 dígito(s) entero(s) y 2 decimal(es)")
    private BigDecimal bodyFatPercentage;

    @Positive(message = "Hombros debe ser mayor que cero")
    @Digits(integer = 3, fraction = 1, message = "Valor no válido para hombros: máximo 3 dígito(s) entero(s) y 1 decimal(es)")
    private BigDecimal shoulders;

    @Positive(message = "Pecho debe ser mayor que cero")
    @Digits(integer = 3, fraction = 1, message = "Valor no válido para pecho: máximo 3 dígito(s) entero(s) y 1 decimal(es)")
    private BigDecimal chest;

    @Positive(message = "Cintura debe ser mayor que cero")
    @Digits(integer = 3, fraction = 1, message = "Valor no válido para cintura: máximo 3 dígito(s) entero(s) y 1 decimal(es)")
    private BigDecimal waist;

    @Positive(message = "Cadera debe ser mayor que cero")
    @Digits(integer = 3, fraction = 1, message = "Valor no válido para cadera: máximo 3 dígito(s) entero(s) y 1 decimal(es)")
    private BigDecimal hip;

    @Positive(message = "Brazo debe ser mayor que cero")
    @Digits(integer = 3, fraction = 1, message = "Valor no válido para brazo: máximo 3 dígito(s) entero(s) y 1 decimal(es)")
    private BigDecimal arm;

    @Positive(message = "Muslo debe ser mayor que cero")
    @Digits(integer = 3, fraction = 1, message = "Valor no válido para muslo: máximo 3 dígito(s) entero(s) y 1 decimal(es)")
    private BigDecimal thigh;

    @Positive(message = "Pantorrilla debe ser mayor que cero")
    @Digits(integer = 3, fraction = 1, message = "Valor no válido para pantorrilla: máximo 3 dígito(s) entero(s) y 1 decimal(es)")
    private BigDecimal calf;

    @NotNull(message = "La fecha de registro es obligatoria")
    private LocalDate recordDate;

}
