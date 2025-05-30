package com.example.To_do_service.Service;

import com.example.To_do_service.model.Tarea;
import com.example.To_do_service.model.TaskStatus;

import java.util.List;
import java.util.Optional;


public interface TareaService {

    Tarea crearTarea(Tarea tarea);

    Optional<Tarea> obtenerTareaPorId(Long id);

    List<Tarea> obtenerTodasLasTareas();

    List<Tarea> obtenerTareasPorEstado(TaskStatus status);

    Tarea actualizarTarea(Long id, Tarea tareaDetalles);

    void eliminarTarea(Long id);


    // Nuevos métodos
    List<Tarea> obtenerTareasPorUsuario(String username);
    List<Tarea> obtenerTareasPorUsuarioYEstado(String username, TaskStatus status);
}
