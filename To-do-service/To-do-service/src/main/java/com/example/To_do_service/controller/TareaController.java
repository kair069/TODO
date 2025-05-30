package com.example.To_do_service.controller;

import com.example.To_do_service.Service.TareaService;
import com.example.To_do_service.model.Tarea;
import com.example.To_do_service.model.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tareas")
@RequiredArgsConstructor
public class TareaController {

    private final TareaService tareaService;

    @PostMapping
    public ResponseEntity<Tarea> crearTarea(@RequestBody Tarea tarea) {
        // Obtener el username del usuario autenticado
        String username = getCurrentUsername();

        // Establecer el username en la tarea antes de guardarla
        tarea.setUsername(username);

        Tarea creada = tareaService.crearTarea(tarea);
        return ResponseEntity.ok(creada);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarea> obtenerTareaPorId(@PathVariable Long id) {
        String username = getCurrentUsername();

        // Obtener la tarea y verificar que pertenezca al usuario actual
        return tareaService.obtenerTareaPorId(id)
                .filter(tarea -> tarea.getUsername() != null && tarea.getUsername().equals(username))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Tarea>> obtenerTodasLasTareas() {
        // En lugar de obtener todas las tareas, solo obtenemos las del usuario actual
        String username = getCurrentUsername();
        List<Tarea> tareas = tareaService.obtenerTareasPorUsuario(username);
        return ResponseEntity.ok(tareas);
    }

    @GetMapping("/estado/{status}")
    public ResponseEntity<List<Tarea>> obtenerTareasPorEstado(@PathVariable TaskStatus status) {
        // Filtrar por estado y por usuario
        String username = getCurrentUsername();
        List<Tarea> tareas = tareaService.obtenerTareasPorUsuarioYEstado(username, status);
        return ResponseEntity.ok(tareas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarea> actualizarTarea(@PathVariable Long id, @RequestBody Tarea tareaDetalles) {
        try {
            String username = getCurrentUsername();

            // Verificar que la tarea pertenezca al usuario actual
            tareaService.obtenerTareaPorId(id)
                    .filter(tarea -> tarea.getUsername() != null && tarea.getUsername().equals(username))
                    .orElseThrow(() -> new RuntimeException("Tarea no encontrada o no autorizada"));

            // Asegurar que no se cambie el propietario
            tareaDetalles.setUsername(username);

            Tarea actualizada = tareaService.actualizarTarea(id, tareaDetalles);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long id) {
        try {
            String username = getCurrentUsername();

            // Verificar que la tarea pertenezca al usuario actual
            tareaService.obtenerTareaPorId(id)
                    .filter(tarea -> tarea.getUsername() != null && tarea.getUsername().equals(username))
                    .orElseThrow(() -> new RuntimeException("Tarea no encontrada o no autorizada"));

            tareaService.eliminarTarea(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }
}