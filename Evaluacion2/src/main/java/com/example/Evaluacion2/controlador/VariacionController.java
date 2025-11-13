package com.example.Evaluacion2.controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Evaluacion2.entidades.Variacion;
import com.example.Evaluacion2.servicio.MuebleService;

@RestController
@RequestMapping("/api/muebles/{muebleId}/variaciones")
public class VariacionController {

    private final MuebleService muebleService;

    public VariacionController(MuebleService muebleService) {
        this.muebleService = muebleService;
    }

    @GetMapping
    public ResponseEntity<List<Variacion>> listarVariaciones(@PathVariable Long muebleId) {
        return muebleService.listarVariaciones(muebleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Variacion> agregarVariacion(@PathVariable Long muebleId, @RequestBody Variacion variacion) {
        return muebleService.agregarVariacion(muebleId, variacion)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{variacionId}")
    public ResponseEntity<Void> eliminarVariacion(@PathVariable Long muebleId, @PathVariable Long variacionId) {
        return muebleService.eliminarVariacion(muebleId, variacionId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
