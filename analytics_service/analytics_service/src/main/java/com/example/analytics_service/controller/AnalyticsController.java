package com.example.analytics_service.controller;

import com.example.analytics_service.client.TodoServiceClient;
import com.example.analytics_service.model.TaskAnalytics;
import com.example.analytics_service.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final TodoServiceClient todoServiceClient;

    /**
     * Endpoint público para probar que funciona
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analytics Service is running!");
    }

    /**
     * Crear estadísticas manualmente (para testing)
     */
    @PostMapping("/crear")
    public ResponseEntity<TaskAnalytics> crearEstadisticas(
            @RequestParam String username,
            @RequestParam(required = false) String fecha,
            @RequestParam int totalTasks,
            @RequestParam int completedTasks,
            @RequestParam int pendingTasks) {

        LocalDate date = fecha != null ? LocalDate.parse(fecha) : LocalDate.now();

        TaskAnalytics analytics = analyticsService.crearEstadisticas(
                username, date, totalTasks, completedTasks, pendingTasks);

        return ResponseEntity.ok(analytics);
    }

    /**
     * Obtener estadísticas de un usuario
     */
    @GetMapping("/usuario/{username}")
    public ResponseEntity<List<TaskAnalytics>> obtenerEstadisticasUsuario(
            @PathVariable String username) {

        List<TaskAnalytics> analytics = analyticsService.obtenerTodasLasEstadisticas(username);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Obtener estadísticas de los últimos 7 días
     */
    @GetMapping("/usuario/{username}/ultimos7dias")
    public ResponseEntity<List<TaskAnalytics>> obtenerUltimos7Dias(
            @PathVariable String username) {

        List<TaskAnalytics> analytics = analyticsService.obtenerUltimos7Dias(username);
        return ResponseEntity.ok(analytics);
    }

    /**
     * NUEVO: Estadísticas en tiempo real desde Todo Service (VERSIÓN SÍNCRONA)
     */
    @GetMapping("/usuario/{username}/tiempo-real-sync")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasTiempoRealSync(
            @PathVariable String username,
            HttpServletRequest request) {

        System.out.println("=== INICIO SYNC - Obteniendo estadísticas para: " + username);

        // Obtener token real del header Authorization
        String token = obtenerTokenDelRequest(request);

        if (token == null) {
            System.out.println("=== ERROR - Token no encontrado");
            return ResponseEntity.status(401).body(Map.of("error", "Token no encontrado"));
        }

        System.out.println("=== Token obtenido correctamente");

        try {
            // Llamada síncrona (bloqueante)
            List<Map<String, Object>> tareas = todoServiceClient.obtenerTareasUsuario(token).block();

            System.out.println("=== Tareas recibidas: " + tareas.size());

            // Calcular estadísticas
            Map<String, Object> estadisticas = calcularEstadisticas(username, tareas);
            System.out.println("=== Estadísticas calculadas: " + estadisticas);

            return ResponseEntity.ok(estadisticas);

        } catch (Exception e) {
            System.out.println("=== ERROR: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Método auxiliar para obtener el token real del request
     */
    private String obtenerTokenDelRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remover "Bearer "
        }
        return null;
    }

    /**
     * Método auxiliar para calcular estadísticas
     */
    private Map<String, Object> calcularEstadisticas(String username, List<Map<String, Object>> tareas) {
        Map<String, Object> stats = new HashMap<>();

        int total = tareas.size();
        int completed = (int) tareas.stream()
                .filter(tarea -> "COMPLETED".equals(tarea.get("status")))
                .count();
        int pending = (int) tareas.stream()
                .filter(tarea -> "PENDING".equals(tarea.get("status")))
                .count();
        int inProgress = (int) tareas.stream()
                .filter(tarea -> "IN_PROGRESS".equals(tarea.get("status")))
                .count();

        double completionRate = total > 0 ? (double) completed / total * 100 : 0.0;

        stats.put("username", username);
        stats.put("fecha", LocalDate.now().toString());
        stats.put("totalTasks", total);
        stats.put("completedTasks", completed);
        stats.put("pendingTasks", pending);
        stats.put("inProgressTasks", inProgress);
        stats.put("completionRate", completionRate);
        stats.put("source", "tiempo-real");

        return stats;
    }

    /**
     * DEMO: Endpoint LENTO para comparación (simula procesamiento tradicional)
     */
    @GetMapping("/usuario/{username}/tradicional")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasTradicional(
            @PathVariable String username,
            HttpServletRequest request) {

        long startTime = System.currentTimeMillis();
        System.out.println("=== DEMO TRADICIONAL - INICIO para: " + username);

        String token = obtenerTokenDelRequest(request);

        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Token no encontrado"));
        }

        try {
            // 🐌 SIMULAMOS PROCESAMIENTO TRADICIONAL LENTO
            Thread.sleep(1000); // 1 segundo de delay artificial

            // Llamada (simulamos RestTemplate lento)
            List<Map<String, Object>> tareas = todoServiceClient.obtenerTareasUsuario(token).block();

            // 🐌 MÁS PROCESAMIENTO LENTO
            Thread.sleep(500); // 500ms más

            Map<String, Object> estadisticas = calcularEstadisticas(username, tareas);

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            // Agregar métricas de tiempo
            estadisticas.put("tiempoTotal", totalTime + "ms");
            estadisticas.put("metodo", "TRADICIONAL-SIMULADO");
            estadisticas.put("nota", "Simula RestTemplate bloqueante + procesamiento lento");

            System.out.println("=== DEMO TRADICIONAL - Completado en: " + totalTime + "ms");

            return ResponseEntity.ok(estadisticas);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}