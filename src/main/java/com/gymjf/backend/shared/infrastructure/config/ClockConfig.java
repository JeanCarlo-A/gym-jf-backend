package com.gymjf.backend.shared.infrastructure.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Reloj del gimnasio. Los vencimientos ("00:00") se calculan en la hora de Costa Rica aunque el servidor
 * (por ejemplo un contenedor Docker) esté en UTC.
 */
@Configuration
@EnableScheduling
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${application.timezone:America/Costa_Rica}") String timezone) {
        return Clock.system(ZoneId.of(timezone));
    }
}
