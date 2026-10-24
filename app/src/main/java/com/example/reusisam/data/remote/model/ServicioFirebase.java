package com.example.reusisam.data.remote.model;

public class ServicioFirebase {

    private String nombre;
    private String descripcion;
    private float costo;
    private boolean estado;
    private String categoriaServicioId;

    public ServicioFirebase() {
    }

    public ServicioFirebase(
            String nombre,
            String descripcion,
            float costo,
            boolean estado,
            String categoriaServicioId
    ) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costo = costo;
        this.estado = estado;
        this.categoriaServicioId = categoriaServicioId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public float getCosto() {
        return costo;
    }

    public void setCosto(float costo) {
        this.costo = costo;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public String getCategoriaServicioId() {
        return categoriaServicioId;
    }

    public void setCategoriaServicioId(String categoriaServicioId) {
        this.categoriaServicioId = categoriaServicioId;
    }
}
