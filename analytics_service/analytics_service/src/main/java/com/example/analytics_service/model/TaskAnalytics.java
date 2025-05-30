package com.example.analytics_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private LocalDate date;  // Fecha para la cual se calculan las estadísticas

    // Contadores simples
    private Integer totalTasks = 0;
    private Integer completedTasks = 0;
    private Integer pendingTasks = 0;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}


//docker run -p 8083:8083 -e SPRING_AUTOCONFIGURE_EXCLUDE=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration -e EUREKA_CLIENT_ENABLED=false analytics-service:latest