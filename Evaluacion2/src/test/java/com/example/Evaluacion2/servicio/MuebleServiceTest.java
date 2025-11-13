package com.example.Evaluacion2.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.Evaluacion2.entidades.Cotizacion;
import com.example.Evaluacion2.entidades.Mueble;
import com.example.Evaluacion2.entidades.Variacion;
import com.example.Evaluacion2.repositorio.CotizacionRepository;
import com.example.Evaluacion2.repositorio.MuebleRepository;
import com.example.Evaluacion2.repositorio.VariacionRepository;

@ExtendWith(MockitoExtension.class)
class MuebleServiceTest {

    @Mock
    private MuebleRepository muebleRepository;

    @Mock
    private VariacionRepository variacionRepository;

    @Mock
    private CotizacionRepository cotizacionRepository;

    @InjectMocks
    private MuebleService muebleService;

    @Test
    void obtenerPrecioFinal_sumaPrecioBaseMasVariaciones() {
        Mueble mueble = crearMueble(1L, 100.0, 5);
        when(muebleRepository.findById(1L)).thenReturn(Optional.of(mueble));
        when(variacionRepository.existsByMuebleIdAndNombreIgnoreCase(1L, "Normal")).thenReturn(true);
        when(variacionRepository.findByMuebleId(1L)).thenReturn(List.of(
                variacionConIncremento(15.0),
                variacionConIncremento(5.0)));

        Optional<Double> total = muebleService.obtenerPrecioFinal(1L);

        assertTrue(total.isPresent());
        assertEquals(120.0, total.get());
        verify(variacionRepository).findByMuebleId(1L);
    }

    @Test
    void crearCotizacion_lanzaErrorCuandoNoHayStock() {
        Mueble sinStock = crearMueble(1L, 200.0, 0);
        when(muebleRepository.findById(1L)).thenReturn(Optional.of(sinStock));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> muebleService.crearCotizacion(List.of(1L)));

        assertEquals("stock insuficiente", exception.getMessage());
        verify(muebleRepository, never()).save(sinStock);
        verify(cotizacionRepository, never()).save(any(Cotizacion.class));
    }

    @Test
    void crearCotizacion_descuentaStockYConfirmaVenta() {
        Mueble mueble = crearMueble(2L, 150.0, 3);
        when(muebleRepository.findById(2L)).thenReturn(Optional.of(mueble));
        when(muebleRepository.save(any(Mueble.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(variacionRepository.findByMuebleId(2L)).thenReturn(List.of(variacionConIncremento(20.0)));
        when(cotizacionRepository.save(any(Cotizacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cotizacion cotizacion = muebleService.crearCotizacion(List.of(2L));

        assertNotNull(cotizacion.getFecha());
        assertEquals(170.0, cotizacion.getTotal());
        assertEquals(1, cotizacion.getMuebles().size());
        assertEquals(2, cotizacion.getMuebles().get(0).getStock());
        verify(muebleRepository).save(argThat(m -> m.getId().equals(2L) && m.getStock() == 2));
        verify(cotizacionRepository).save(any(Cotizacion.class));
    }

    @Test
    void crear_asignaActivoPorDefectoYAseguraVariacionBasica() {
        Mueble nuevo = crearMueble(null, 80.0, 10);
        nuevo.setActivo(null);
        when(muebleRepository.save(any(Mueble.class))).thenAnswer(invocation -> {
            Mueble guardado = invocation.getArgument(0);
            guardado.setId(10L);
            return guardado;
        });
        when(variacionRepository.existsByMuebleIdAndNombreIgnoreCase(10L, "Normal")).thenReturn(false);
        when(variacionRepository.save(any(Variacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mueble resultado = muebleService.crear(nuevo);

        assertTrue(resultado.getActivo());
        verify(variacionRepository).save(argThat(var -> "Normal".equals(var.getNombre())
                && var.getIncrementoPrecio() == 0.0
                && var.getMueble().getId().equals(10L)));
    }

    @Test
    void actualizar_modificaDatosDelCatalogo() {
        Mueble existente = crearMueble(5L, 100.0, 4);
        when(muebleRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(muebleRepository.save(any(Mueble.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mueble cambios = crearMueble(null, 180.0, 8);
        cambios.setNombre("Nuevo nombre");
        cambios.setMaterial("Metal");

        Optional<Mueble> actualizado = muebleService.actualizar(5L, cambios);

        assertTrue(actualizado.isPresent());
        assertEquals("Nuevo nombre", actualizado.get().getNombre());
        assertEquals(180.0, actualizado.get().getPrecioBase());
        assertEquals("Metal", actualizado.get().getMaterial());
        verify(muebleRepository).save(existente);
    }

    @Test
    void desactivar_inactivaElMueble() {
        Mueble existente = crearMueble(7L, 90.0, 2);
        existente.setActivo(true);
        when(muebleRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(muebleRepository.save(any(Mueble.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Mueble> resultado = muebleService.desactivar(7L);

        assertTrue(resultado.isPresent());
        assertFalse(resultado.get().getActivo());
        verify(muebleRepository).save(argThat(m -> Boolean.FALSE.equals(m.getActivo())));
    }

    private Mueble crearMueble(Long id, Double precioBase, Integer stock) {
        Mueble mueble = new Mueble();
        mueble.setId(id);
        mueble.setNombre("Mueble " + (id == null ? "nuevo" : id));
        mueble.setTipo("Silla");
        mueble.setPrecioBase(precioBase);
        mueble.setStock(stock);
        mueble.setActivo(true);
        mueble.setTamano("Mediano");
        mueble.setMaterial("Madera");
        return mueble;
    }

    private Variacion variacionConIncremento(Double incremento) {
        Variacion variacion = new Variacion();
        variacion.setIncrementoPrecio(incremento);
        return variacion;
    }
}
