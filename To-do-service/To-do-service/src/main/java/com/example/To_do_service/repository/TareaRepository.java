package com.example.To_do_service.repository;

import com.example.To_do_service.model.Tarea;
import com.example.To_do_service.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {
    List<Tarea> findByStatus(TaskStatus status);


    List<Tarea> findByUsername(String username);
    List<Tarea> findByUsernameAndStatus(String username, TaskStatus status);
}
