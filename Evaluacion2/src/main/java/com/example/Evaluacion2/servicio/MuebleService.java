package com.example.Evaluacion2.servicio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Evaluacion2.entidades.Cotizacion;
import com.example.Evaluacion2.entidades.Mueble;
import com.example.Evaluacion2.entidades.Variacion;
import com.example.Evaluacion2.repositorio.CotizacionRepository;
import com.example.Evaluacion2.repositorio.MuebleRepository;
import com.example.Evaluacion2.repositorio.VariacionRepository;

@Service
public class MuebleService {

    private static final String VARIACION_NORMAL = "Normal";

    private final MuebleRepository muebleRepository;
    private final VariacionRepository variacionRepository;
    private final CotizacionRepository cotizacionRepository;

    public MuebleService(MuebleRepository muebleRepository,
            VariacionRepository variacionRepository,
            CotizacionRepository cotizacionRepository) {
        this.muebleRepository = muebleRepository;
        this.variacionRepository = variacionRepository;
        this.cotizacionRepository = cotizacionRepository;
    }

    public List<Mueble> listarTodos() {
        return muebleRepository.findAll();
    }

    public List<Mueble> listarActivos() {
        return muebleRepository.findByActivoTrue();
    }

    public Optional<Mueble> buscarPorId(Long id) {
        return muebleRepository.findById(id);
    }

    public Mueble crear(Mueble mueble) {
        // Por defecto activo
        if (mueble.getActivo() == null) {
            mueble.setActivo(true);
        }
        Mueble guardado = muebleRepository.save(mueble);
        asegurarVariacionNormal(guardado);
        return guardado;
    }

    public Optional<Mueble> actualizar(Long id, Mueble datos) {
        return muebleRepository.findById(id).map(m -> {
            m.setNombre(datos.getNombre());
            m.setTipo(datos.getTipo());
            m.setPrecioBase(datos.getPrecioBase());
            m.setStock(datos.getStock());
            m.setTamano(datos.getTamano());
            m.setMaterial(datos.getMaterial());
            // Permitimos cambiar estado desde el body también
            m.setActivo(datos.getActivo());
            return muebleRepository.save(m);
        });
    }

    public Optional<Mueble> desactivar(Long id) {
        return muebleRepository.findById(id).map(m -> {
            m.setActivo(false);
            return muebleRepository.save(m);
        });
    }

    public Optional<List<Variacion>> listarVariaciones(Long muebleId) {
        return muebleRepository.findById(muebleId).map(mueble -> {
            asegurarVariacionNormal(mueble);
            return variacionRepository.findByMuebleId(muebleId);
        });
    }

    public Optional<Variacion> agregarVariacion(Long muebleId, Variacion variacion) {
        return muebleRepository.findById(muebleId).map(mueble -> {
            variacion.setId(null);
            if (variacion.getIncrementoPrecio() == null) {
                variacion.setIncrementoPrecio(0.0);
            }
            eliminarVariacionNormalSiExiste(muebleId);
            variacion.setMueble(mueble);
            return variacionRepository.save(variacion);
        });
    }

    public boolean eliminarVariacion(Long muebleId, Long variacionId) {
        return variacionRepository.findById(variacionId)
                .filter(v -> v.getMueble().getId().equals(muebleId))
                .map(v -> {
                    variacionRepository.delete(v);
                    reponerVariacionNormalSiNoHayVariaciones(muebleId);
                    return true;
                })
                .orElse(false);
    }

    public Optional<Double> obtenerPrecioFinal(Long muebleId) {
        return muebleRepository.findById(muebleId)
                .map(mueble -> {
                    asegurarVariacionNormal(mueble);
                    return calcularPrecioFinal(mueble);
                });
    }

    @Transactional
    public Cotizacion crearCotizacion(List<Long> muebleIds) {
        if (muebleIds == null || muebleIds.isEmpty()) {
            throw new IllegalArgumentException("Debe incluir al menos un mueble");
        }

        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setFecha(LocalDateTime.now());
        double total = 0.0;

        for (Long muebleId : muebleIds) {
            Mueble mueble = muebleRepository.findById(muebleId)
                    .orElseThrow(() -> new IllegalArgumentException("Mueble no encontrado: " + muebleId));

            if (mueble.getStock() == null || mueble.getStock() <= 0) {
                throw new IllegalStateException("stock insuficiente");
            }

            mueble.setStock(mueble.getStock() - 1);
            muebleRepository.save(mueble);

            total += calcularPrecioFinal(mueble);
            cotizacion.getMuebles().add(mueble);
        }

        cotizacion.setTotal(total);
        return cotizacionRepository.save(cotizacion);
    }

    private double calcularPrecioFinal(Mueble mueble) {
        double precioBase = Optional.ofNullable(mueble.getPrecioBase()).orElse(0.0);
        double incrementos = variacionRepository.findByMuebleId(mueble.getId()).stream()
                .mapToDouble(v -> Optional.ofNullable(v.getIncrementoPrecio()).orElse(0.0))
                .sum();
        return precioBase + incrementos;
    }

    private void asegurarVariacionNormal(Mueble mueble) {
        if (mueble == null || mueble.getId() == null) {
            return;
        }
        if (!variacionRepository.existsByMuebleIdAndNombreIgnoreCase(mueble.getId(), VARIACION_NORMAL)) {
            Variacion variacionNormal = new Variacion();
            variacionNormal.setNombre(VARIACION_NORMAL);
            variacionNormal.setIncrementoPrecio(0.0);
            variacionNormal.setMueble(mueble);
            variacionRepository.save(variacionNormal);
        }
    }

    private void eliminarVariacionNormalSiExiste(Long muebleId) {
        variacionRepository.findFirstByMuebleIdAndNombreIgnoreCase(muebleId, VARIACION_NORMAL)
                .ifPresent(variacionRepository::delete);
    }

    private void reponerVariacionNormalSiNoHayVariaciones(Long muebleId) {
        if (variacionRepository.countByMuebleId(muebleId) == 0) {
            muebleRepository.findById(muebleId).ifPresent(this::asegurarVariacionNormal);
        }
    }
}
