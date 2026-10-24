package com.example.reusisam.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "usuarios",
        indices = {@Index(value = {"email"}, unique = true)}
)
public class Usuario {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String nombre;

    @NonNull
    private String email;

    @NonNull
    private String password;

    public Usuario(@NonNull String nombre, @NonNull String email, @NonNull String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
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
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }
}
