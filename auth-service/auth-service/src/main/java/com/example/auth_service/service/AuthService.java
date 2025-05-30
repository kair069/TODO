//package com.example.auth_service.service;
//
//import com.example.auth_service.dto.AuthResponse;
//import com.example.auth_service.dto.LoginRequest;
//import com.example.auth_service.dto.RegisterRequest;
//import com.example.auth_service.exception.InvalidCredentialsException;
//import com.example.auth_service.exception.UserAlreadyExistsException;
//
//import com.example.auth_service.model.Role;
//import com.example.auth_service.model.User;
//import com.example.auth_service.repository.RoleRepository;
//import com.example.auth_service.repository.UserRepository;
//import com.example.auth_service.security.JwtService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Collections;
//
//@Service
//public class AuthService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private RoleRepository roleRepository;
//
//    @Autowired
//    private JwtService jwtService;
//
//    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    public AuthResponse login(LoginRequest request) {
//        try {
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
//            );
//
//            User user = (User) authentication.getPrincipal();
//            String token = jwtService.generateToken(user);
//            return new AuthResponse(token, user.getUsername(),
//                    "¡Bienvenido de nuevo, " + user.getUsername() + "! Has iniciado sesión exitosamente.");
//        } catch (BadCredentialsException e) {
//            throw new InvalidCredentialsException("Credenciales inválidas. Por favor, verifica tu nombre de usuario y contraseña.");
//        }
//    }
//
//
//    @Transactional
//    public AuthResponse register(RegisterRequest request) {
//        // Validar que el username no esté vacío
//        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
//            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
//        }
//
//        // Validar que la contraseña no esté vacía y tenga al menos 6 caracteres
//        if (request.getPassword() == null || request.getPassword().length() < 6) {
//            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
//        }
//
//        // Verificar si el usuario ya existe
//        if (userRepository.existsByUsername(request.getUsername())) {
//            throw new UserAlreadyExistsException("El usuario '" + request.getUsername() + "' ya está registrado");
//        }
//
//        // Obtener o crear rol ROLE_USER
//        Role userRole = roleRepository.findByName("ROLE_USER")
//                .orElseGet(() -> {
//                    Role newRole = new Role();
//                    newRole.setName("ROLE_USER");
//                    return roleRepository.save(newRole);
//                });
//
//        // Crear nuevo usuario
//        User user = new User();
//        user.setUsername(request.getUsername());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        user.setRoles(Collections.singleton(userRole));
//        userRepository.save(user);
//
//        // Generar token
//        String token = jwtService.generateToken(user);
//
//        // Crear respuesta con mensaje
//        AuthResponse response = new AuthResponse(token, user.getUsername());
//        response.setMessage("¡Registro exitoso! Bienvenido, " + user.getUsername() + ".");
//
//        return response;
//    }
//}

package com.example.auth_service.service;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.exception.InvalidCredentialsException;
import com.example.auth_service.exception.UserAlreadyExistsException;

import com.example.auth_service.model.Role;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//@Service
//public class AuthService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private RoleRepository roleRepository;
//
//    @Autowired
//    private JwtService jwtService;
//
//    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    // TU MÉTODO LOGIN ORIGINAL - SIN CAMBIOS
//    public AuthResponse login(LoginRequest request) {
//        try {
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
//            );
//
//            User user = (User) authentication.getPrincipal();
//            String token = jwtService.generateToken(user);
//            return new AuthResponse(token, user.getUsername(),
//                    "¡Bienvenido de nuevo, " + user.getUsername() + "! Has iniciado sesión exitosamente.");
//        } catch (BadCredentialsException e) {
//            throw new InvalidCredentialsException("Credenciales inválidas. Por favor, verifica tu nombre de usuario y contraseña.");
//        }
//    }
//
//    @Transactional
//    public AuthResponse register(RegisterRequest request) {
//        // NUEVAS VALIDACIONES CON LAMBDAS Y STREAMS
//        List<String> validationErrors = Stream.of(
//                        validateUsername(request.getUsername()),
//                        validatePassword(request.getPassword())
//                        // validateEmail(request.getEmail()) // Descomenta si tienes email en RegisterRequest
//                )
//                .filter(error -> !error.isEmpty())        // Lambda aquí
//                .collect(Collectors.toList());           // Stream aquí
//
//        // Si hay errores, los lanzamos unidos
//        if (!validationErrors.isEmpty()) {
//            throw new IllegalArgumentException(String.join(", ", validationErrors));
//        }
//
//        // RESTO DE TU CÓDIGO ORIGINAL - SIN CAMBIOS
//        // Verificar si el usuario ya existe
//        if (userRepository.existsByUsername(request.getUsername())) {
//            throw new UserAlreadyExistsException("El usuario '" + request.getUsername() + "' ya está registrado");
//        }
//
//        // Obtener o crear rol ROLE_USER (TU LAMBDA ORIGINAL)
//        Role userRole = roleRepository.findByName("ROLE_USER")
//                .orElseGet(() -> {
//                    Role newRole = new Role();
//                    newRole.setName("ROLE_USER");
//                    return roleRepository.save(newRole);
//                });
//
//        // Crear nuevo usuario
//        User user = new User();
//        user.setUsername(request.getUsername());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        user.setRoles(Collections.singleton(userRole));
//        userRepository.save(user);
//
//        // Generar token
//        String token = jwtService.generateToken(user);
//
//        // Crear respuesta con mensaje
//        AuthResponse response = new AuthResponse(token, user.getUsername());
//        response.setMessage("¡Registro exitoso! Bienvenido, " + user.getUsername() + ".");
//
//        return response;
//    }
//
//    // NUEVOS MÉTODOS DE VALIDACIÓN CON LAMBDAS
//    private String validateUsername(String username) {
//        return (username == null || username.trim().isEmpty())
//                ? "El nombre de usuario no puede estar vacío" : "";
//    }
//
//    private String validatePassword(String password) {
//        return (password == null || password.length() < 6)
//                ? "La contraseña debe tener al menos 6 caracteres" : "";
//    }
//
//    // Método adicional por si tienes email en el futuro
//    private String validateEmail(String email) {
//        if (email == null || email.trim().isEmpty()) {
//            return ""; // Email opcional, no requerido
//        }
//        return email.contains("@") ? "" : "Email debe contener @";
//    }
//}
/**
 * Servicio de Autenticación - Maneja login y registro de usuarios
 *
 * Esta clase es el núcleo de la seguridad de la aplicación.
 * Se encarga de:
 * - Autenticar usuarios existentes (login)
 * - Registrar nuevos usuarios
 * - Generar tokens JWT
 * - Validar credenciales y datos de entrada
 */
@Service // Indica que es un componente de servicio de Spring
public class AuthService {

    // ==================== DEPENDENCIAS INYECTADAS ====================

    /**
     * Repositorio para operaciones CRUD con la entidad User
     * Permite buscar, guardar y verificar existencia de usuarios
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Repositorio para manejar roles de usuario (ROLE_USER, ROLE_ADMIN, etc.)
     */
    @Autowired
    private RoleRepository roleRepository;

    /**
     * Servicio personalizado para generar y validar tokens JWT
     * JWT = JSON Web Token (para mantener sesiones sin estado)
     */
    @Autowired
    private JwtService jwtService;

    /**
     * Manager de Spring Security que se encarga de la autenticación
     * Verifica credenciales contra la base de datos
     */
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Codificador de contraseñas (BCrypt típicamente)
     * NUNCA se guardan contraseñas en texto plano - siempre encriptadas
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== MÉTODO LOGIN ====================

    /**
     * Autentica un usuario existente y genera un token JWT
     *
     * @param request Objeto que contiene username y password
     * @return AuthResponse con token JWT y mensaje de bienvenida
     * @throws InvalidCredentialsException si las credenciales son incorrectas
     */
    public AuthResponse login(LoginRequest request) {
        try {
            // PASO 1: Intentar autenticar con Spring Security
            // UsernamePasswordAuthenticationToken es el formato que entiende Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // PASO 2: Si llegamos aquí, la autenticación fue exitosa
            // Extraemos el usuario autenticado del objeto Authentication
            User user = (User) authentication.getPrincipal();

            // PASO 3: Generar token JWT para este usuario
            // Este token será usado en requests posteriores para mantener la sesión
            String token = jwtService.generateToken(user);

            // PASO 4: Crear respuesta exitosa con token y mensaje personalizado
            return new AuthResponse(token, user.getUsername(),
                    "¡Bienvenido de nuevo, " + user.getUsername() + "! Has iniciado sesión exitosamente.");

        } catch (BadCredentialsException e) {
            // MANEJO DE ERROR: Credenciales incorrectas
            // Convertimos la excepción técnica en una más amigable para el usuario
            throw new InvalidCredentialsException("Credenciales inválidas. Por favor, verifica tu nombre de usuario y contraseña.");
        }
    }

    // ==================== MÉTODO REGISTER ====================

    /**
     * Registra un nuevo usuario en el sistema
     *
     * @Transactional: Si algo falla durante el registro, se hace rollback automático
     * Esto asegura que no queden datos inconsistentes en la base de datos
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // PASO 1: VALIDACIONES CON PROGRAMACIÓN FUNCIONAL (LAMBDAS Y STREAMS)
        // Este es un enfoque moderno y elegante para validar múltiples campos
        List<String> validationErrors = Stream.of(
                        validateUsername(request.getUsername()),    // Valida username
                        validatePassword(request.getPassword())     // Valida password
                        // validateEmail(request.getEmail())        // Opcional: valida email
                )
                .filter(error -> !error.isEmpty())        // LAMBDA: Solo mantiene errores no vacíos
                .collect(Collectors.toList());             // STREAM: Convierte a lista

        // Si encontramos errores de validación, detenemos el proceso
        if (!validationErrors.isEmpty()) {
            // Unimos todos los errores en un solo mensaje separado por comas
            throw new IllegalArgumentException(String.join(", ", validationErrors));
        }

        // PASO 2: VERIFICAR QUE EL USUARIO NO EXISTA YA
        // Prevenir duplicados en la base de datos
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("El usuario '" + request.getUsername() + "' ya está registrado");
        }

        // PASO 3: OBTENER O CREAR ROL POR DEFECTO
        // Esta es una LAMBDA muy elegante que:
        // - Busca el rol "ROLE_USER"
        // - Si no existe, lo crea automáticamente
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {                    // LAMBDA: Se ejecuta solo si no se encuentra el rol
                    Role newRole = new Role();        // Crear nuevo rol
                    newRole.setName("ROLE_USER");     // Asignar nombre
                    return roleRepository.save(newRole); // Guardar y retornar
                });

        // PASO 4: CREAR Y CONFIGURAR NUEVO USUARIO
        User user = new User();
        user.setUsername(request.getUsername());
        // IMPORTANTE: La contraseña se encripta SIEMPRE antes de guardarla
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Asignar rol por defecto (normalmente ROLE_USER para usuarios regulares)
        user.setRoles(Collections.singleton(userRole));

        // PASO 5: GUARDAR USUARIO EN BASE DE DATOS
        userRepository.save(user);

        // PASO 6: GENERAR TOKEN JWT PARA LOGIN AUTOMÁTICO
        // Después del registro, el usuario queda logueado automáticamente
        String token = jwtService.generateToken(user);

        // PASO 7: CREAR RESPUESTA EXITOSA
        AuthResponse response = new AuthResponse(token, user.getUsername());
        response.setMessage("¡Registro exitoso! Bienvenido, " + user.getUsername() + ".");

        return response;
    }

    // ==================== MÉTODOS DE VALIDACIÓN PRIVADOS ====================

    /**
     * Valida que el username no esté vacío o nulo
     *
     * ENFOQUE FUNCIONAL: Usa operador ternario para validación concisa
     * @param username Username a validar
     * @return String vacío si es válido, mensaje de error si no es válido
     */
    private String validateUsername(String username) {
        return (username == null || username.trim().isEmpty())
                ? "El nombre de usuario no puede estar vacío"  // Error
                : "";                                          // Sin error
    }

    /**
     * Valida que la contraseña tenga al menos 6 caracteres
     *
     * @param password Contraseña a validar
     * @return String vacío si es válida, mensaje de error si no es válida
     */
    private String validatePassword(String password) {
        return (password == null || password.length() < 6)
                ? "La contraseña debe tener al menos 6 caracteres"  // Error
                : "";                                               // Sin error
    }

    /**
     * Método adicional para validar email (preparado para futuras funcionalidades)
     *
     * @param email Email a validar (puede ser null - es opcional)
     * @return String vacío si es válido, mensaje de error si no es válido
     */
    private String validateEmail(String email) {
        // Si no hay email, no es error (campo opcional)
        if (email == null || email.trim().isEmpty()) {
            return "";
        }
        // Validación básica: debe contener @
        return email.contains("@") ? "" : "Email debe contener @";
    }
}

/*
==================== CONCEPTOS CLAVE PARA LA ENTREVISTA ====================

1. **@Service**: Estereotipo de Spring para la capa de lógica de negocio
2. **@Autowired**: Inyección de dependencias automática de Spring
3. **@Transactional**: Manejo automático de transacciones de base de datos
4. **JWT**: Tokens sin estado para autenticación en APIs REST
5. **PasswordEncoder**: Encriptación segura de contraseñas (BCrypt)
6. **AuthenticationManager**: Núcleo de Spring Security para autenticación
7. **Lambdas y Streams**: Programación funcional moderna en Java 8+
8. **Optional.orElseGet()**: Manejo elegante de valores opcionales
9. **Exception Handling**: Conversión de excepciones técnicas a amigables
10. **Validation Pattern**: Separación de validaciones en métodos privados

==================== POSIBLES PREGUNTAS DE ENTREVISTA ====================

Q: "¿Por qué usas @Transactional en register?"
A: "Para asegurar atomicidad. Si falla algún paso del registro,
   se hace rollback automático y no quedan datos inconsistentes."

Q: "¿Por qué no guardas contraseñas en texto plano?"
A: "Por seguridad. Uso PasswordEncoder con BCrypt para encriptar.
   Así, aunque hackeen la DB, no pueden ver contraseñas reales."

Q: "Explica el uso de Streams en las validaciones."
A: "Stream.of() crea un stream, filter() mantiene solo errores no vacíos,
   collect() convierte a lista. Es más funcional y legible que loops."

Q: "¿Qué es JWT y por qué lo usas?"
A: "JSON Web Token. Es stateless, perfecto para APIs REST.
   El servidor no guarda sesiones, todo está en el token."
*/