package com.example.Evaluacion2.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Evaluacion2.entidades.Mueble;

public interface MuebleRepository extends JpaRepository<Mueble, Long> {

    List<Mueble> findByActivoTrue();
}