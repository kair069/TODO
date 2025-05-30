package com.example.analytics_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.Collections;

//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    // IMPORTANTE: Usar la MISMA clave secreta que en tu auth-service y todo-service
//    private static final String SECRET_KEY = "mi-clave-super-secreta-mas-larga-que-256-bits-segura";
//
//    private Key getSigningKey() {
//        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
//    }
//
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain) throws ServletException, IOException {
//
//        final String authHeader = request.getHeader("Authorization");
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        try {
//            final String jwt = authHeader.substring(7);
//            final String username = extractUsername(jwt);
//
//            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                // Crear autenticación
//                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                        username,
//                        null,
//                        Collections.emptyList()
//                );
//
//                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authToken);
//            }
//        } catch (Exception e) {
//            logger.error("Error validando token JWT", e);
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private String extractUsername(String token) {
//        Claims claims = Jwts.parserBuilder()
//                .setSigningKey(getSigningKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//        return claims.getSubject();
//    }
//}
/**
 * FILTRO DE AUTENTICACIÓN JWT - CORAZÓN DE LA SEGURIDAD
 * ====================================================
 *
 * Este filtro intercepta TODAS las peticiones HTTP y verifica tokens JWT.
 * Es parte fundamental del pipeline de Spring Security.
 *
 * ¿QUÉ HACE?
 * - Intercepta cada request HTTP antes de llegar al controller
 * - Extrae y valida tokens JWT del header Authorization
 * - Establece el contexto de seguridad si el token es válido
 * - Permite o bloquea el acceso basado en autenticación
 *
 * FLUJO:
 * HTTP Request → JwtAuthenticationFilter → [Otros Filtros] → Controller
 *
 * ¿POR QUÉ OncePerRequestFilter?
 * - Garantiza que el filtro se ejecute UNA SOLA VEZ por request
 * - Evita duplicación en forwards/includes internos
 * - Mejor rendimiento que implementar Filter directamente
 */
@Component // Spring component - se registra automáticamente en el container
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // ==================== CONFIGURACIÓN DE SEGURIDAD JWT ====================

    /**
     * CLAVE SECRETA PARA FIRMAR/VERIFICAR TOKENS JWT
     * =============================================
     *
     * ⚠️  CRÍTICO PARA SEGURIDAD:
     * - Esta clave DEBE ser la MISMA en todos los microservicios
     * - Debe tener al menos 256 bits (32 caracteres) para HMAC-SHA256
     * - En producción: usar variables de entorno o vault de secretos
     * - NUNCA hardcodear en código de producción
     *
     * MECANISMO:
     * 1. Auth Service firma tokens con esta clave
     * 2. Otros servicios verifican tokens con la MISMA clave
     * 3. Si las claves no coinciden = tokens inválidos
     */
    private static final String SECRET_KEY = "mi-clave-super-secreta-mas-larga-que-256-bits-segura";

    /**
     * GENERA LA CLAVE CRIPTOGRÁFICA PARA JWT
     * ====================================
     *
     * Convierte el string SECRET_KEY en un objeto Key que JJWT puede usar.
     * HMAC-SHA256 requiere claves de al menos 256 bits.
     *
     * @return Key objeto criptográfico para firmar/verificar
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // ==================== FILTRO PRINCIPAL ====================

    /**
     * MÉTODO PRINCIPAL DEL FILTRO - SE EJECUTA EN CADA REQUEST
     * =======================================================
     *
     * Este método intercepta TODAS las peticiones HTTP y:
     * 1. Busca el header "Authorization"
     * 2. Extrae el token JWT
     * 3. Valida el token y extrae el username
     * 4. Establece el contexto de autenticación en Spring Security
     * 5. Permite que el request continúe
     *
     * @param request Petición HTTP entrante
     * @param response Respuesta HTTP (para modificar si es necesario)
     * @param filterChain Cadena de filtros de Spring Security
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // PASO 1: EXTRAER HEADER DE AUTORIZACIÓN
        // Buscar: "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        final String authHeader = request.getHeader("Authorization");

        // PASO 2: VALIDACIÓN BÁSICA DEL HEADER
        // Si no hay header o no empieza con "Bearer ", continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // NO HAY TOKEN → Continuar (endpoints públicos siguen funcionando)
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // PASO 3: EXTRAER TOKEN JWT
            // "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." → "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            final String jwt = authHeader.substring(7); // Quitar "Bearer "

            // PASO 4: EXTRAER USERNAME DEL TOKEN
            // Decodifica el JWT y extrae el "subject" (username)
            final String username = extractUsername(jwt);

            // PASO 5: VERIFICAR SI NECESITAMOS AUTENTICAR
            // Si hay username válido Y no hay autenticación previa en este request
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // PASO 6: CREAR OBJETO DE AUTENTICACIÓN
                // Esto le dice a Spring Security que el usuario está autenticado
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,                    // Principal (quien es el usuario)
                        null,                       // Credentials (no necesarias con JWT)
                        Collections.emptyList()     // Authorities (roles/permisos - vacío por ahora)
                        // TODO: Podrías extraer roles del JWT y pasarlos aquí
                );

                // PASO 7: AGREGAR DETALLES DEL REQUEST
                // Información adicional (IP, session ID, etc.) para auditoría
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // PASO 8: ESTABLECER AUTENTICACIÓN EN SPRING SECURITY
                // ¡CRÍTICO! Esto hace que SecurityContextHolder.getContext().getAuthentication()
                // devuelva este token en el resto del request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception e) {
            // MANEJO DE ERRORES: Token malformado, expirado, firma inválida, etc.
            // Loggeamos el error pero NO bloqueamos el request
            // El endpoint decidirá si requiere autenticación o no
            logger.error("Error validando token JWT", e);
            // NO hacer return aquí - dejar que el request continúe
        }

        // PASO 9: CONTINUAR CON LA CADENA DE FILTROS
        // Sea exitoso o no, el request debe continuar
        // Otros filtros o el controller decidirán qué hacer
        filterChain.doFilter(request, response);
    }

    // ==================== UTILIDADES JWT ====================


//     //* EXTRAE EL USERNAME (SUBJECT) DE UN TOKEN JWT
//     //* ==========================================
//     *
//     * Proceso de validación:
//     * 1. Parsea el token JWT usando la clave secreta
//     * 2. Verifica la firma criptográfica
//     * 3. Verifica que no haya expirado
//     * 4. Extrae el "subject" (username) del payload
//     *
//     * @param token Token JWT como string
//     * @return String username del token
//     * @throws JwtException si el token es inválido, expirado o mal firmado

    private String extractUsername(String token) {
        // PARSEO Y VALIDACIÓN COMPLETA DEL TOKEN
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())     // Usar la misma clave para verificar
                .build()                           // Construir el parser
                .parseClaimsJws(token)             // Parsear y VALIDAR el token
                .getBody();                        // Obtener el payload (claims)

        // EXTRAER EL SUBJECT (USERNAME)
        return claims.getSubject();
    }

    /*
    ==================== MÉTODOS ADICIONALES QUE PODRÍAS AGREGAR ====================
    */

    /**
     * EXTRAER ROLES DEL TOKEN JWT (ejemplo)
     */
    /*
    private List<SimpleGrantedAuthority> extractAuthorities(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Asumir que roles están en el claim "roles" como ["ROLE_USER", "ROLE_ADMIN"]
        List<String> roles = claims.get("roles", List.class);

        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
    */

    /**
     * VERIFICAR SI EL TOKEN HA EXPIRADO
     */
    /*
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }
    */
}

/*
==================== FLUJO COMPLETO DE SEGURIDAD ====================

1. REQUEST LLEGA:
   GET /api/protected-endpoint
   Header: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

2. JWTAUTHENTICATIONFILTER:
   - Extrae token del header
   - Valida firma y expiración
   - Extrae username del token
   - Crea UsernamePasswordAuthenticationToken
   - Lo guarda en SecurityContextHolder

3. SPRING SECURITY:
   - Otros filtros ven que hay autenticación
   - @PreAuthorize, @Secured funcionan automáticamente
   - SecurityContextHolder.getContext().getAuthentication() devuelve datos

4. CONTROLLER:
   - Puede acceder a usuario autenticado
   - @AuthenticationPrincipal String username
   - SecurityContextHolder.getContext().getAuthentication().getName()

==================== CONCEPTOS CLAVE PARA LA ENTREVISTA ====================

1. **OncePerRequestFilter**: Se ejecuta una sola vez por request
2. **SecurityContextHolder**: Almacena información de autenticación del hilo actual
3. **UsernamePasswordAuthenticationToken**: Representa autenticación exitosa
4. **FilterChain**: Patrón Chain of Responsibility en Spring Security
5. **JWT Claims**: Payload del token con información del usuario
6. **HMAC-SHA256**: Algoritmo criptográfico para firmar tokens
7. **Bearer Token**: Estándar para enviar tokens en headers HTTP

==================== POSIBLES PREGUNTAS DE ENTREVISTA ====================

Q: "¿Por qué heredas de OncePerRequestFilter y no implementas Filter?"
A: "OncePerRequestFilter garantiza ejecución única por request,
   maneja forwards/includes correctamente, y tiene mejor rendimiento."

Q: "¿Qué pasa si el token es inválido?"
A: "Se loggea el error pero el request continúa. El endpoint decidirá
   si requiere autenticación. Endpoints públicos siguen funcionando."

Q: "¿Por qué no usas UserDetailsService en este filtro?"
A: "Con JWT no necesito consultar la BD. El token ya contiene la info.
   Es stateless - no necesito cargar usuario de BD cada vez."

Q: "¿Cómo manejarías refresh tokens?"
A: "Tendría un endpoint /auth/refresh que acepte refresh token,
   valide que no esté revocado, y genere nuevo access token."

Q: "¿Dónde se ejecuta este filtro en la cadena de Spring Security?"
A: "Se configura ANTES de UsernamePasswordAuthenticationFilter
   en SecurityConfig, para interceptar requests con JWT primero."

==================== ORDEN DE FILTROS EN SPRING SECURITY ====================

HTTP Request
     │
     ▼
┌─────────────────┐
│SecurityContext  │ ← Limpia contexto al final
│PersistenceFilter│
└─────────────────┘
     │
     ▼
┌─────────────────┐
│JwtAuthentication│ ← ¡TU FILTRO AQUÍ!
│Filter           │
└─────────────────┘
     │
     ▼
┌─────────────────┐
│UsernamePassword │ ← Para formularios de login
│AuthFilter       │
└─────────────────┘
     │
     ▼
┌─────────────────┐
│Authorization    │ ← Verifica permisos
│Filter           │
└─────────────────┘
     │
     ▼
Controller/Endpoint

==================== CONFIGURACIÓN EN SECURITYCONFIG ====================

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()  // Endpoints públicos
                .anyRequest().authenticated()             // Todo lo demás requiere autenticación
            )
            .build();
    }
}
*/