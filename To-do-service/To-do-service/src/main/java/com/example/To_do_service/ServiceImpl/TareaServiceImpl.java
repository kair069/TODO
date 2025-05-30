package com.example.To_do_service.ServiceImpl;

import com.example.To_do_service.Service.TareaService;
import com.example.To_do_service.model.Tarea;
import com.example.To_do_service.model.TaskStatus;
import com.example.To_do_service.repository.TareaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class TareaServiceImpl implements TareaService {

    private final TareaRepository tareaRepository;

    @Override
    public Tarea crearTarea(Tarea tarea) {
        return tareaRepository.save(tarea);
    }

    @Override
    public Optional<Tarea> obtenerTareaPorId(Long id) {
        return tareaRepository.findById(id);
    }

    @Override
    public List<Tarea> obtenerTodasLasTareas() {
        return tareaRepository.findAll();
    }

    @Override
    public List<Tarea> obtenerTareasPorEstado(TaskStatus status) {
        return tareaRepository.findByStatus(status);
    }

    @Override
    public Tarea actualizarTarea(Long id, Tarea tareaDetalles) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada con id: " + id));

        tarea.setTitle(tareaDetalles.getTitle());
        tarea.setDescription(tareaDetalles.getDescription());
        tarea.setStatus(tareaDetalles.getStatus());

        return tareaRepository.save(tarea);
    }

    @Override
    public void eliminarTarea(Long id) {
        tareaRepository.deleteById(id);
    }


    // Implementaciones existentes...

    @Override
    public List<Tarea> obtenerTareasPorUsuario(String username) {
        return tareaRepository.findByUsername(username);
    }

    @Override
    public List<Tarea> obtenerTareasPorUsuarioYEstado(String username, TaskStatus status) {
        return tareaRepository.findByUsernameAndStatus(username, status);
    }
}
