package com.example.analytics_service.repository;

import com.example.analytics_service.model.TaskAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAnalyticsRepository extends JpaRepository<TaskAnalytics, Long> {

    // Buscar estadísticas por usuario y fecha específica
    Optional<TaskAnalytics> findByUsernameAndDate(String username, LocalDate date);

    // Buscar todas las estadísticas de un usuario
    List<TaskAnalytics> findByUsernameOrderByDateDesc(String username);

    // Buscar estadísticas de los últimos días para un usuario
    List<TaskAnalytics> findTop7ByUsernameOrderByDateDesc(String username);
}