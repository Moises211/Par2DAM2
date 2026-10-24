package com.example.reusisam;

public class CategoriaDTO {
    private final String firestoreId;
    private String nombre;

    public CategoriaDTO(String firestoreId, String nombre) {
        this.firestoreId = firestoreId;
        this.nombre = nombre;
    }

    public String getFirestoreId() {
        return firestoreId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
