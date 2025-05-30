package com.example.analytics_service.service;

import com.example.analytics_service.model.TaskAnalytics;
import com.example.analytics_service.repository.TaskAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final TaskAnalyticsRepository analyticsRepository;

    /**
     * Crear estadísticas simples para un usuario
     */
    public TaskAnalytics crearEstadisticas(String username, LocalDate fecha,
                                           int totalTasks, int completedTasks, int pendingTasks) {
        log.info("Creando estadísticas para usuario: {} en fecha: {}", username, fecha);

        TaskAnalytics analytics = TaskAnalytics.builder()
                .username(username)
                .date(fecha)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .build();

        return analyticsRepository.save(analytics);
    }

    /**
     * Obtener estadísticas por usuario y fecha
     */
    public Optional<TaskAnalytics> obtenerEstadisticas(String username, LocalDate fecha) {
        log.info("Obteniendo estadísticas para usuario: {} en fecha: {}", username, fecha);
        return analyticsRepository.findByUsernameAndDate(username, fecha);
    }

    /**
     * Obtener todas las estadísticas de un usuario
     */
    public List<TaskAnalytics> obtenerTodasLasEstadisticas(String username) {
        log.info("Obteniendo todas las estadísticas para usuario: {}", username);
        return analyticsRepository.findByUsernameOrderByDateDesc(username);
    }

    /**
     * Obtener estadísticas de los últimos 7 días
     */
    public List<TaskAnalytics> obtenerUltimos7Dias(String username) {
        log.info("Obteniendo estadísticas de los últimos 7 días para usuario: {}", username);
        return analyticsRepository.findTop7ByUsernameOrderByDateDesc(username);
    }
}