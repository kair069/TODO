package com.example.To_do_service.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PENDING;

    private LocalDateTime createdAt;

    // Simplemente agregar este campo, sin relaciones JPA
    private String username;  // Usamos el username en lugar del ID para mayor claridad


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
