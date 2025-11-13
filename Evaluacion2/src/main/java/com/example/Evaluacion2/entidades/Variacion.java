package com.example.Evaluacion2.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "variaciones")
public class Variacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "incremento_precio", nullable = false)
    private Double incrementoPrecio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mueble_id")
    @JsonIgnore
    private Mueble mueble;

    public Variacion() {
    }

    public Variacion(String nombre, Double incrementoPrecio) {
        this.nombre = nombre;
        this.incrementoPrecio = incrementoPrecio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getIncrementoPrecio() {
        return incrementoPrecio;
    }

    public void setIncrementoPrecio(Double incrementoPrecio) {
        this.incrementoPrecio = incrementoPrecio;
    }

    public Mueble getMueble() {
        return mueble;
    }

    public void setMueble(Mueble mueble) {
        this.mueble = mueble;
    }
}
