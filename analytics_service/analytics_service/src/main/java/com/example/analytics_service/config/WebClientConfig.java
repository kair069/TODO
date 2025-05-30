package com.example.analytics_service.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * CONFIGURACIÓN DE WEBCLIENT PARA MICROSERVICIOS
 * =============================================
 *
 * Esta clase configura WebClient para comunicación entre microservicios
 * con capacidades de Load Balancing automático.
 *
 * ¿QUÉ PROBLEMA RESUELVE?
 * - Permite llamadas HTTP reactivas entre microservicios
 * - Distribuye automáticamente las peticiones entre múltiples instancias
 * - Reemplaza RestTemplate (síncrono) con WebClient (asíncrono/reactivo)
 */
@Configuration // Indica que esta clase contiene configuraciones de Spring
public class WebClientConfig {

    /**
     * WEBCLIENT BUILDER CON LOAD BALANCER
     * ===================================
     *
     * Este Bean es el corazón de la comunicación entre microservicios.
     *
     * ¿QUÉ HACE @LoadBalanced?
     * - Intercepta las URLs con nombres de servicio (ej: "http://user-service/api/users")
     * - Resuelve automáticamente a IPs reales usando Service Discovery
     * - Distribuye peticiones entre múltiples instancias del mismo servicio
     * - Maneja failover automático si una instancia falla
     *
     * EJEMPLO DE USO:
     * WebClient client = webClientBuilder.build();
     * client.get()
     *       .uri("http://user-service/api/users/123")  // Nombre lógico, no IP
     *       .retrieve()
     *       .bodyToMono(User.class);
     */
    @Bean // Registra este método como un Bean de Spring
    @LoadBalanced // ¡MAGIA! Habilita load balancing automático
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                // Aquí podrías agregar configuraciones adicionales:

                // .defaultHeader("Content-Type", "application/json")
                // .defaultHeader("Accept", "application/json")

                // .codecs(configurer -> configurer
                //     .defaultCodecs()
                //     .maxInMemorySize(1024 * 1024)) // 1MB buffer

                // .filter(ExchangeFilterFunction.ofRequestProcessor(
                //     request -> {
                //         System.out.println("Request: " + request.url());
                //         return Mono.just(request);
                //     }))
                ;
    }

    /*
    ==================== CONFIGURACIONES ADICIONALES OPCIONALES ====================

    Si necesitas más configuraciones avanzadas, podrías agregar:
    */

    /**
     * WEBCLIENT CONFIGURADO ESPECÍFICAMENTE PARA UN SERVICIO
     * (Ejemplo: si necesitas configuraciones específicas para user-service)
     */
    /*
    @Bean("userServiceWebClient")
    public WebClient userServiceWebClient(@LoadBalanced WebClient.Builder builder) {
        return builder
            .baseUrl("http://user-service")  // URL base del servicio
            .defaultHeader("Service-Name", "auth-service")  // Header personalizado
            .build();
    }
    */

    /**
     * TIMEOUT Y CONFIGURACIONES DE CONEXIÓN
     * (Para controlar timeouts y pool de conexiones)
     */
    /*
    @Bean
    public WebClient webClientWithTimeout(@LoadBalanced WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)  // 5 segundos timeout
            .responseTimeout(Duration.ofMillis(5000))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

        return builder
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
    */
}

/*
==================== CONCEPTOS CLAVE PARA LA ENTREVISTA ====================

1. **@Configuration**: Clase que define beans y configuraciones de Spring
2. **@Bean**: Método que produce un objeto gestionado por Spring Container
3. **@LoadBalanced**: Anotación de Spring Cloud para habilitar load balancing
4. **WebClient**: Cliente HTTP reactivo (reemplazo moderno de RestTemplate)
5. **Service Discovery**: Mecanismo para encontrar servicios por nombre lógico
6. **Load Balancing**: Distribución automática de carga entre instancias

==================== ARQUITECTURA DE MICROSERVICIOS ====================

SIN @LoadBalanced:
┌─────────────┐    HTTP Request     ┌─────────────┐
│ Auth Service│ ─────────────────▶ │192.168.1.10 │
└─────────────┘   (IP hardcodeada) └─────────────┘

CON @LoadBalanced:
┌─────────────┐    "user-service"   ┌──────────────┐    ┌─────────────┐
│ Auth Service│ ─────────────────▶ │Load Balancer │───▶│192.168.1.10 │
└─────────────┘   (nombre lógico)   │   (Ribbon)   │    └─────────────┘
                                    └──────────────┘    ┌─────────────┐
                                                       │192.168.1.11 │
                                                       └─────────────┘

==================== POSIBLES PREGUNTAS DE ENTREVISTA ====================

Q: "¿Por qué WebClient en lugar de RestTemplate?"
A: "WebClient es reactivo (no bloquea hilos), soporta programación
   asíncrona, mejor rendimiento, y es el estándar moderno de Spring."

Q: "¿Cómo funciona @LoadBalanced?"
A: "Intercepta las URLs con nombres de servicio, usa Service Discovery
   (Eureka/Consul) para resolver IPs, y distribuye peticiones automáticamente."

Q: "¿Qué pasa si un microservicio está caído?"
A: "El Load Balancer detecta la falla, quita la instancia del pool,
   redirige tráfico a instancias sanas, y reintenta automáticamente."

Q: "¿Cómo usarías este WebClient en un servicio?"
A: "Inyecto WebClient.Builder, construyo el cliente, y hago llamadas:
   webClientBuilder.build().get().uri('http://servicio/endpoint')
   .retrieve().bodyToMono(ClaseRespuesta.class)"

 */