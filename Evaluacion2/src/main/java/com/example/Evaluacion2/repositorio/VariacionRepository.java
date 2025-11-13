package com.example.Evaluacion2.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Evaluacion2.entidades.Variacion;

public interface VariacionRepository extends JpaRepository<Variacion, Long> {

    List<Variacion> findByMuebleId(Long muebleId);

    boolean existsByMuebleIdAndNombreIgnoreCase(Long muebleId, String nombre);

    Optional<Variacion> findFirstByMuebleIdAndNombreIgnoreCase(Long muebleId, String nombre);

    long countByMuebleId(Long muebleId);
}
