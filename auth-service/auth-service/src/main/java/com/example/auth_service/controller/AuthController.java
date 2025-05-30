package com.example.auth_service.controller;

import com.example.auth_service.dto.AuthResponse;

import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.errores.ErrorResponse;
import com.example.auth_service.exception.InvalidCredentialsException;
import com.example.auth_service.exception.InvalidTokenException;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.JwtService;
import com.example.auth_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

//@RestController
//@RequestMapping("/auth")
//public class AuthController {
//
//    @Autowired
//    private AuthService authService;
//
//    @Autowired
//    private JwtService jwtService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @PostMapping("/register")
//    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
//        try {
//            AuthResponse response = authService.register(request);
//            return ResponseEntity.status(HttpStatus.CREATED).body(response);
//        } catch (IllegalArgumentException e) {
//            ErrorResponse errorResponse = new ErrorResponse(
//                    HttpStatus.BAD_REQUEST.value(),
//                    "Bad Request",
//                    e.getMessage(),
//                    "/auth/register"
//            );
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//        }
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
//        System.out.println("Solicitud de login recibida para: " + request.getUsername());
//        try {
//            AuthResponse response = authService.login(request);
//            System.out.println("Login exitoso para: " + request.getUsername());
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            System.err.println("Error durante el login: " + e.getMessage());
//            e.printStackTrace();
//            // Manejar excepción y devolver respuesta de error
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
//                    new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Unauthorized",
//                            e.getMessage(), "/auth/login"));
//        }
//    }
//
//    @GetMapping("/validate")
//    public ResponseEntity<?> validateToken(@RequestParam String token) {
//        try {
//            if (token == null || token.isEmpty()) {
//                throw new InvalidTokenException("El token no puede estar vacío");
//            }
//
//            String username = jwtService.extractUsername(token);
//            if (username == null) {
//                throw new InvalidTokenException("Token inválido o mal formado");
//            }
//
//            User user = userRepository.findByUsername(username)
//                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + username));
//
//            boolean isValid = jwtService.isTokenValid(token, user);
//            if (!isValid) {
//                throw new InvalidTokenException("Token expirado o inválido");
//            }
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("valid", true);
//            response.put("username", username);
//
//            return ResponseEntity.ok(response);
//        } catch (InvalidTokenException | UserNotFoundException e) {
//            ErrorResponse errorResponse = new ErrorResponse(
//                    HttpStatus.UNAUTHORIZED.value(),
//                    "Unauthorized",
//                    e.getMessage(),
//                    "/auth/validate"
//            );
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
//        } catch (Exception e) {
//            ErrorResponse errorResponse = new ErrorResponse(
//                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                    "Internal Server Error",
//                    "Error al validar el token: " + e.getMessage(),
//                    "/auth/validate"
//            );
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//        }
//    }
//}

/**
 * CONTROLADOR DE AUTENTICACIÓN - API REST
 * =====================================
 *
 * Este controlador expone los endpoints públicos para:
 * - Registro de nuevos usuarios
 * - Login de usuarios existentes
 * - Validación de tokens JWT
 *
 * Es la PUERTA DE ENTRADA de tu sistema de autenticación.
 * Todos los requests de auth pasan por aquí.
 */
@RestController // Combina @Controller + @ResponseBody (respuestas automáticas en JSON)
@RequestMapping("/auth") // Base URL: todos los endpoints empiezan con /auth
public class AuthController {

    // ==================== DEPENDENCIAS INYECTADAS ====================

    /**
     * Servicio que contiene la lógica de negocio de autenticación
     * (El AuthService que ya comentamos anteriormente)
     */
    @Autowired
    private AuthService authService;

    /**
     * Servicio para manejar tokens JWT
     * (Extraer username, validar expiración, etc.)
     */
    @Autowired
    private JwtService jwtService;

    /**
     * Repositorio para buscar usuarios en base de datos
     * (Usado específicamente en el endpoint de validación)
     */
    @Autowired
    private UserRepository userRepository;

    // ==================== ENDPOINT: REGISTRO DE USUARIOS ====================

    /**
     * ENDPOINT: POST /auth/register
     * ============================
     *
     * Registra un nuevo usuario en el sistema.
     *
     * FLUJO:
     * 1. Cliente envía JSON con datos de registro
     * 2. Se validan los datos (username, password)
     * 3. Se crea el usuario en BD con contraseña encriptada
     * 4. Se genera token JWT automáticamente
     * 5. Usuario queda logueado después del registro
     *
     * RESPUESTAS:
     * - 201 CREATED: Registro exitoso + token JWT
     * - 400 BAD REQUEST: Datos inválidos o usuario ya existe
     */
    @PostMapping("/register") // POST /auth/register
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // DELEGAMOS la lógica al servicio (separación de responsabilidades)
            AuthResponse response = authService.register(request);

            // ÉXITO: Código 201 (CREATED) para recursos nuevos
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            // ERROR DE VALIDACIÓN: Datos de entrada incorrectos
            // Creamos respuesta de error estructurada y consistente
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),    // 400
                    "Bad Request",                     // Status text
                    e.getMessage(),                    // Mensaje específico del error
                    "/auth/register"                   // Endpoint donde ocurrió
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        // NOTA: Otras excepciones (UserAlreadyExistsException) se manejan
        // en un @ControllerAdvice global (si lo tienes)
    }

    // ==================== ENDPOINT: LOGIN DE USUARIOS ====================

    /**
     * ENDPOINT: POST /auth/login
     * =========================
     *
     * Autentica un usuario existente y genera token JWT.
     *
     * FLUJO:
     * 1. Cliente envía username + password
     * 2. Spring Security valida credenciales
     * 3. Si son correctas, genera token JWT
     * 4. Cliente usa este token en requests posteriores
     *
     * RESPUESTAS:
     * - 200 OK: Login exitoso + token JWT
     * - 401 UNAUTHORIZED: Credenciales incorrectas
     */
    @PostMapping("/login") // POST /auth/login
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // LOG DE DEPURACIÓN: Para monitorear intentos de login
        // TODO: En producción, usar un logger profesional (SLF4J + Logback)
        System.out.println("Solicitud de login recibida para: " + request.getUsername());

        try {
            // DELEGAMOS al servicio la lógica de autenticación
            AuthResponse response = authService.login(request);

            // LOG DE ÉXITO
            System.out.println("Login exitoso para: " + request.getUsername());

            // RESPUESTA EXITOSA: 200 OK + token JWT
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // MANEJO DE ERRORES CON LOGS DETALLADOS
            System.err.println("Error durante el login: " + e.getMessage());
            e.printStackTrace(); // Stack trace completo para debugging

            // RESPUESTA DE ERROR: 401 UNAUTHORIZED
            // Estructura consistente para todos los errores
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ErrorResponse(
                            HttpStatus.UNAUTHORIZED.value(),  // 401
                            "Unauthorized",                   // Status text
                            e.getMessage(),                   // Mensaje específico
                            "/auth/login"                     // Endpoint
                    )
            );
        }
    }

    // ==================== ENDPOINT: VALIDACIÓN DE TOKENS ====================

    /**
     * ENDPOINT: GET /auth/validate?token=xxxxx
     * ======================================
     *
     * Valida si un token JWT es válido y no ha expirado.
     *
     * ¿CUÁNDO SE USA?
     * - Otros microservicios verifican tokens
     * - Frontend verifica si el usuario sigue logueado
     * - API Gateway valida permisos antes de rutear requests
     *
     * FLUJO DETALLADO:
     * 1. Verificar que el token no esté vacío
     * 2. Extraer username del token
     * 3. Buscar usuario en base de datos
     * 4. Verificar que el token no haya expirado
     * 5. Devolver información del usuario si todo es válido
     *
     * RESPUESTAS:
     * - 200 OK: Token válido + info del usuario
     * - 401 UNAUTHORIZED: Token inválido/expirado/usuario no existe
     * - 500 INTERNAL_SERVER_ERROR: Error inesperado del servidor
     */
    @GetMapping("/validate") // GET /auth/validate?token=xxxxx
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            // PASO 1: VALIDACIÓN BÁSICA DEL TOKEN
            if (token == null || token.isEmpty()) {
                throw new InvalidTokenException("El token no puede estar vacío");
            }

            // PASO 2: EXTRAER USERNAME DEL TOKEN JWT
            // El token contiene información codificada (payload con username, exp, etc.)
            String username = jwtService.extractUsername(token);
            if (username == null) {
                throw new InvalidTokenException("Token inválido o mal formado");
            }

            // PASO 3: VERIFICAR QUE EL USUARIO EXISTA EN LA BASE DE DATOS
            // Importante: el token podría ser válido pero el usuario podría haber sido eliminado
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + username));

            // PASO 4: VALIDACIÓN COMPLETA DEL TOKEN
            // Verifica: firma válida + no expirado + usuario correcto
            boolean isValid = jwtService.isTokenValid(token, user);
            if (!isValid) {
                throw new InvalidTokenException("Token expirado o inválido");
            }

            // PASO 5: CREAR RESPUESTA EXITOSA
            // Devolvemos información útil para el cliente
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);           // Confirmación de validez
            response.put("username", username);    // Username del token
            // Podrías agregar: roles, expiración, etc.

            return ResponseEntity.ok(response);

        } catch (InvalidTokenException | UserNotFoundException e) {
            // ERRORES ESPERADOS: Token malo o usuario no existe
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),  // 401
                    "Unauthorized",
                    e.getMessage(),
                    "/auth/validate"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            // ERRORES INESPERADOS: Problemas de BD, parsing, etc.
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),  // 500
                    "Internal Server Error",
                    "Error al validar el token: " + e.getMessage(),
                    "/auth/validate"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

/*
==================== CONCEPTOS CLAVE PARA LA ENTREVISTA ====================

1. **@RestController**: Controlador que devuelve datos JSON (no vistas HTML)
2. **@RequestMapping**: Define la URL base para todos los endpoints de la clase
3. **@PostMapping/@GetMapping**: Mapean HTTP methods específicos
4. **@RequestBody**: Convierte JSON automáticamente a objetos Java
5. **@RequestParam**: Captura parámetros de query string (?token=xxx)
6. **ResponseEntity<?>**: Permite controlar HTTP status codes y headers
7. **Exception Handling**: Manejo robusto de errores con try-catch
8. **Separation of Concerns**: Controller delega lógica al Service

==================== ARQUITECTURA REST ====================

CLIENT                    CONTROLLER                   SERVICE                    DATABASE
┌─────┐   HTTP Request   ┌────────────┐   Method Call  ┌─────────┐   SQL Query   ┌────────┐
│ APP │ ──────────────▶ │AuthController│ ────────────▶ │AuthService│ ────────────▶│Database│
└─────┘                 └────────────┘                └─────────┘                └────────┘
  ▲                           │                           │                           │
  └─── JSON Response ─────────┘                           │                           │
       (AuthResponse/                                     │                           │
        ErrorResponse)                                    │                           │
                                                         └─── Data Access ────────────┘

==================== POSIBLES PREGUNTAS DE ENTREVISTA ====================

Q: "¿Por qué usas ResponseEntity<?> en lugar de devolver objetos directamente?"
A: "ResponseEntity me permite controlar HTTP status codes (200, 201, 400, 401),
   headers, y el body. Es más profesional para APIs REST."

Q: "¿Por qué separas la lógica en AuthService en lugar de ponerla en el Controller?"
A: "Separation of Concerns. Controller maneja HTTP, Service maneja lógica de negocio.
   Así el Service es reutilizable y testeable independientemente."

Q: "¿Cómo manejarías la validación de entrada de forma más elegante?"
A: "Con @Valid y Bean Validation (@NotBlank, @Size, etc.) en RegisterRequest/LoginRequest.
   También un @ControllerAdvice global para manejar excepciones."

Q: "¿Por qué el endpoint de validación usa GET en lugar de POST?"
A: "GET es idempotente y apropiado para operaciones de consulta.
   El token va como query param, no modifica estado del servidor."

Q: "¿Cómo mejorarías el logging en este código?"
A: "Usaría SLF4J + Logback, levels apropiados (INFO, ERROR),
   MDC para request tracing, y evitaría System.out.println."

==================== MEJORAS SUGERIDAS ====================

1. **Logging Profesional**:
   private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

2. **Validación con Annotations**:
   public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request)

3. **Global Exception Handler**:
   @ControllerAdvice para manejo centralizado de excepciones

4. **API Documentation**:
   @Operation, @ApiResponse de Swagger/OpenAPI

5. **Rate Limiting**:
   Para prevenir ataques de fuerza bruta en login
*/