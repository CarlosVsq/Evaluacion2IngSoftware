package com.example.Evaluacion2.controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Evaluacion2.dto.CotizacionRequest;
import com.example.Evaluacion2.entidades.Cotizacion;
import com.example.Evaluacion2.servicio.MuebleService;

@RestController
@RequestMapping("/api/cotizaciones")
public class CotizacionController {

    private final MuebleService muebleService;

    public CotizacionController(MuebleService muebleService) {
        this.muebleService = muebleService;
    }

    @PostMapping
    public ResponseEntity<?> crearCotizacion(@RequestBody CotizacionRequest request) {
        try {
            Cotizacion cotizacion = muebleService.crearCotizacion(request.getMuebleIds());
            return ResponseEntity.ok(cotizacion);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
