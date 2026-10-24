package com.example.reusisam.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorias_servicio")
public class CategoriaServicio {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String nombre;

    public CategoriaServicio(@NonNull String nombre) {
        this.nombre = nombre;
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
}
