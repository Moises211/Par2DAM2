package com.example.reusisam.data.remote.model;

/**
 * Modelo de documento para la colección usuarios de Firestore.
 * Firebase requiere un constructor sin argumentos para deserializar.
 */
public class UsuarioFirebase {

    private String nombre;
    private String email;
    private String password;

    public UsuarioFirebase() {
    }

    public UsuarioFirebase(String nombre, String email, String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
