package com.example.Evaluacion2.controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Evaluacion2.entidades.Mueble;
import com.example.Evaluacion2.servicio.MuebleService;

@RestController
@RequestMapping("/api/muebles")
public class MuebleController {

    private final MuebleService muebleService;

    public MuebleController(MuebleService muebleService) {
        this.muebleService = muebleService;
    }

    @GetMapping
    public List<Mueble> listarTodos() {
        return muebleService.listarTodos();
    }

    @GetMapping("/activos")
    public List<Mueble> listarActivos() {
        return muebleService.listarActivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mueble> obtenerPorId(@PathVariable Long id) {
        return muebleService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Mueble> crear(@RequestBody Mueble mueble) {
        Mueble creado = muebleService.crear(mueble);
        return ResponseEntity.ok(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mueble> actualizar(@PathVariable Long id, @RequestBody Mueble mueble) {
        return muebleService.actualizar(id, mueble)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Mueble> desactivar(@PathVariable Long id) {
        return muebleService.desactivar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/precio-final")
    public ResponseEntity<Double> obtenerPrecioFinal(@PathVariable Long id) {
        return muebleService.obtenerPrecioFinal(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
