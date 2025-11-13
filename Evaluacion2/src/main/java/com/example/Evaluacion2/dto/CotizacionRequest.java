package com.example.Evaluacion2.dto;

import java.util.List;

public class CotizacionRequest {

    private List<Long> muebleIds;

    public List<Long> getMuebleIds() {
        return muebleIds;
    }

    public void setMuebleIds(List<Long> muebleIds) {
        this.muebleIds = muebleIds;
    }
}
