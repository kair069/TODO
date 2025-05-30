package com.example.analytics_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class TodoServiceClient {
//
//    private final WebClient.Builder webClientBuilder;
//
//    /**
//     * Obtiene todas las tareas de un usuario desde el Todo Service
//     */
//    public Mono<List<Map<String, Object>>> obtenerTareasUsuario(String token) {
//        log.info("Consultando tareas del usuario en Todo Service");
//
//        return webClientBuilder.build()
//                .get()
//                .uri("http://todo-service/api/tareas")  // Nombre del servicio en Eureka
//                .header("Authorization", "Bearer " + token)
//                .retrieve()
//                .bodyToFlux(Map.class)
//                .map(map -> (Map<String, Object>) map)  // Cast explícito
//                .collectList()
//                .timeout(Duration.ofSeconds(10))
//                .doOnSuccess(tareas -> log.info("Obtenidas {} tareas del Todo Service", tareas.size()))
//                .doOnError(error -> log.error("Error consultando Todo Service: {}", error.getMessage()))
//                .onErrorReturn(List.of()); // Retorna lista vacía si hay error
//    }
//
//    /**
//     * Verifica si el Todo Service está disponible
//     */
//    public Mono<Boolean> verificarDisponibilidad() {
//        return webClientBuilder.build()
//                .get()
//                .uri("http://todo-service/api/tareas")
//                .header("Authorization", "Bearer dummy") // Token dummy para test
//                .retrieve()
//                .toBodilessEntity()
//                .map(response -> true)
//                .timeout(Duration.ofSeconds(5))
//                .onErrorReturn(false);
//    }
//}

/**
 * CLIENTE PARA COMUNICACIÓN CON TODO-SERVICE
 * =========================================
 *
 * Esta clase es un CLIENT PATTERN para comunicarse con el microservicio Todo.
 * Encapsula toda la lógica de comunicación HTTP y manejo de errores.
 *
 * VENTAJAS DE ESTE PATRÓN:
 * - Centraliza la comunicación con un microservicio específico
 * - Abstrae la complejidad de WebClient para otros servicios
 * - Maneja errores y timeouts de forma consistente
 * - Facilita testing con mocks
 * - Permite evolucionar la API del servicio externo sin afectar el código cliente
 */
@Service // Componente de Spring - lógica de negocio para comunicación entre servicios
@RequiredArgsConstructor // Lombok: Constructor automático con campos final (inyección por constructor)
@Slf4j // Lombok: Logger automático - log.info(), log.error(), etc.
public class TodoServiceClient {

    // ==================== DEPENDENCIAS ====================

    /**
     * WebClient Builder con @LoadBalanced configurado
     *
     * ¿POR QUÉ final?
     * - Inmutable después de la construcción
     * - @RequiredArgsConstructor crea constructor automáticamente
     * - Inyección por constructor (mejor práctica vs @Autowired)
     */
    private final WebClient.Builder webClientBuilder;

    // ==================== MÉTODO: OBTENER TAREAS DEL USUARIO ====================

    /**
     * OBTIENE TODAS LAS TAREAS DE UN USUARIO DESDE TODO-SERVICE
     * ========================================================
     *
     * Este método demuestra varios conceptos avanzados:
     * - Programación reactiva con Mono/Flux
     * - Comunicación entre microservicios con load balancing
     * - Manejo robusto de errores y timeouts
     * - Autenticación JWT entre servicios
     * - Logging estructurado para observabilidad
     *
     * @param token JWT token del usuario autenticado
     * @return Mono<List<Map<String, Object>>> Lista reactiva de tareas
     */
    public Mono<List<Map<String, Object>>> obtenerTareasUsuario(String token) {

        // LOG DE INICIO: Importante para tracing y debugging
        log.info("Consultando tareas del usuario en Todo Service");

        return webClientBuilder.build()  // Construir WebClient con load balancing

                // ==================== HTTP REQUEST SETUP ====================
                .get()  // Método HTTP GET
                .uri("http://todo-service/api/tareas")  // ¡NOMBRE LÓGICO! No IP hardcodeada

                // AUTENTICACIÓN: Pasamos el JWT del usuario a Todo Service
                // Esto permite que Todo Service sepa qué usuario está pidiendo sus tareas
                .header("Authorization", "Bearer " + token)

                // ==================== PROCESAMIENTO REACTIVO ====================
                .retrieve()  // Ejecutar request y obtener respuesta

                // CONVERSIÓN DE DATOS REACTIVA:
                .bodyToFlux(Map.class)  // JSON Array → Flux<Map> (stream de objetos)
                .map(map -> (Map<String, Object>) map)  // Cast explícito para tipo seguro
                .collectList()  // Flux<Map> → Mono<List<Map>> (agrupar stream en lista)

                // ==================== RESILIENCIA Y OBSERVABILIDAD ====================

                // TIMEOUT: Evita que el request cuelgue indefinidamente
                .timeout(Duration.ofSeconds(10))

                // LOGGING DE ÉXITO: Para monitoreo y métricas
                .doOnSuccess(tareas ->
                        log.info("Obtenidas {} tareas del Todo Service", tareas.size())
                )

                // LOGGING DE ERROR: Para debugging y alertas
                .doOnError(error ->
                        log.error("Error consultando Todo Service: {}", error.getMessage())
                )

                // FALLBACK: Si falla, devolver lista vacía (graceful degradation)
                // Esto evita que el Auth Service falle completamente si Todo Service está caído
                .onErrorReturn(List.of());
    }

    // ==================== MÉTODO: HEALTH CHECK ====================

    /**
     * VERIFICA SI TODO-SERVICE ESTÁ DISPONIBLE
     * =======================================
     *
     * Health check para circuit breaker patterns o dashboard de salud.
     *
     * CASOS DE USO:
     * - Dashboard de estado de microservicios
     * - Circuit breaker para evitar llamadas a servicio caído
     * - Métricas de disponibilidad
     * - Tests de integración
     *
     * @return Mono<Boolean> true si está disponible, false si no
     */
    public Mono<Boolean> verificarDisponibilidad() {
        return webClientBuilder.build()
                .get()
                .uri("http://todo-service/api/tareas")

                // TOKEN DUMMY: Solo queremos verificar conectividad, no datos reales
                .header("Authorization", "Bearer dummy")

                .retrieve()
                .toBodilessEntity()  // No necesitamos el body, solo el status code

                // Si llegamos aquí sin excepción = servicio disponible
                .map(response -> true)

                // TIMEOUT MÁS CORTO: Health checks deben ser rápidos
                .timeout(Duration.ofSeconds(5))

                // CUALQUIER ERROR = servicio no disponible
                .onErrorReturn(false);
    }

    /*
    ==================== MÉTODOS ADICIONALES QUE PODRÍAS AGREGAR ====================

    Si quisieras expandir esta clase, podrías agregar:
    */

    /**
     * CREAR NUEVA TAREA (ejemplo de POST)
     */
    /*
    public Mono<Map<String, Object>> crearTarea(String token, Map<String, Object> nuevaTarea) {
        log.info("Creando nueva tarea en Todo Service");

        return webClientBuilder.build()
                .post()
                .uri("http://todo-service/api/tareas")
                .header("Authorization", "Bearer " + token)
                .bodyValue(nuevaTarea)  // Enviar JSON en el body
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .doOnSuccess(tarea -> log.info("Tarea creada exitosamente: {}", tarea.get("id")))
                .doOnError(error -> log.error("Error creando tarea: {}", error.getMessage()));
    }
    */

    /**
     * OBTENER ESTADÍSTICAS DEL USUARIO
     */
    /*
    public Mono<Map<String, Object>> obtenerEstadisticas(String token) {
        return webClientBuilder.build()
                .get()
                .uri("http://todo-service/api/estadisticas")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorReturn(Map.of("error", "Estadísticas no disponibles"));
    }
    */
}

/*
==================== PATRONES DE DISEÑO IMPLEMENTADOS ====================

1. **CLIENT PATTERN**: Encapsula comunicación con servicio externo
2. **CIRCUIT BREAKER**: onErrorReturn() previene cascadas de fallos
3. **TIMEOUT PATTERN**: timeout() evita requests que cuelguen
4. **GRACEFUL DEGRADATION**: Devuelve datos por defecto si falla
5. **OBSERVABILITY**: Logging estructurado para monitoreo

==================== CONCEPTOS CLAVE PARA LA ENTREVISTA ====================

1. **@RequiredArgsConstructor**: Constructor automático con campos final
2. **@Slf4j**: Logger automático de Lombok
3. **Mono/Flux**: Programación reactiva (Project Reactor)
4. **Load Balancing**: "http://todo-service" se resuelve automáticamente
5. **JWT Propagation**: Pasar token entre microservicios
6. **Reactive Streams**: bodyToFlux() → map() → collectList()
7. **Error Handling**: onErrorReturn() para resiliencia
8. **Timeout**: Evitar requests que cuelguen indefinidamente

==================== FLUJO DE COMUNICACIÓN ENTRE MICROSERVICIOS ====================

CLIENT REQUEST          AUTH SERVICE           TODO SERVICE           DATABASE
┌─────────────┐        ┌──────────────┐       ┌──────────────┐       ┌────────┐
│GET /api/user│───────▶│TodoServiceClient│────▶│GET /api/tareas│──────▶│Tasks   │
│+ JWT Token  │        │                │     │+ JWT Token    │       │Table   │
└─────────────┘        └──────────────┘       └──────────────┘       └────────┘
       ▲                        │                       │                   │
       │                        │                       │                   │
       └── JSON Response ───────┘                       │                   │
           (List of Tasks)                               │                   │
                                                        │                   │
                                         ┌──────────────┘                   │
                                         ▼                                  │
                                   ┌──────────────┐                        │
                                   │Load Balancer │                        │
                                   │(Ribbon/Eureka)│                       │
                                   └──────────────┘                        │
                                         │                                  │
                                         └─── Service Discovery ────────────┘

==================== POSIBLES PREGUNTAS DE ENTREVISTA ====================

Q: "¿Por qué usas Mono<List> en lugar de Flux directamente?"
A: "Flux es un stream infinito, pero necesito una lista completa para
   el cliente. collectList() agrupa todo el Flux en una lista."

Q: "¿Qué pasa si Todo Service está caído?"
A: "onErrorReturn(List.of()) devuelve lista vacía. El Auth Service
   sigue funcionando, solo sin datos de tareas (graceful degradation)."

Q: "¿Por qué timeout de 10 segundos para tareas y 5 para health check?"
A: "Health checks deben ser rápidos. Obtener tareas puede tomar más tiempo
   si hay muchos datos, pero 10s es un límite razonable."

Q: "¿Cómo testearías este cliente?"
A: "MockWebServer para simular Todo Service, verificar headers,
   timeouts, y casos de error. TestContainers para tests de integración."

Q: "¿Cómo implementarías retry logic?"
A: "Con .retry(3) o .retryWhen() para reintentos exponenciales.
   También circuit breaker con Resilience4j."

==================== EJEMPLO DE USO EN OTRO SERVICIO ====================

@RestController
public class UserController {

    private final TodoServiceClient todoClient;

    @GetMapping("/api/user/dashboard")
    public Mono<ResponseEntity<Map<String, Object>>> getDashboard(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");

        return todoClient.obtenerTareasUsuario(token)
                .map(tareas -> {
                    Map<String, Object> dashboard = new HashMap<>();
                    dashboard.put("totalTareas", tareas.size());
                    dashboard.put("tareas", tareas);
                    return ResponseEntity.ok(dashboard);
                });
    }
}
*/