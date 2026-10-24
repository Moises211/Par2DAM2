package com.example.reusisam.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "servicios",
        foreignKeys = @ForeignKey(
                entity = CategoriaServicio.class,
                parentColumns = "id",
                childColumns = "categoriaServicioId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("categoriaServicioId")}
)
public class Servicio {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String nombre;

    @NonNull
    private String descripcion;

    private float costo;
    private boolean estado;
    private long categoriaServicioId;

    public Servicio(
            @NonNull String nombre,
            @NonNull String descripcion,
            float costo,
            boolean estado,
            long categoriaServicioId
    ) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costo = costo;
        this.estado = estado;
        this.categoriaServicioId = categoriaServicioId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getNombre() {
        return nombre;
    }

    public void setNombre(@NonNull String nombre) {
        this.nombre = nombre;
    }

    @NonNull
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(@NonNull String descripcion) {
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

    public long getCategoriaServicioId() {
        return categoriaServicioId;
    }

    public void setCategoriaServicioId(long categoriaServicioId) {
        this.categoriaServicioId = categoriaServicioId;
    }
}
