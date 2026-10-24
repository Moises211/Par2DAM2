package com.example.reusisam.DTOS;

import java.util.Map;

public class RecordDTO {
    public final boolean estado ;
    public final Map<String, String> valores;
    public final String clave;

    public RecordDTO (boolean estado, Map valores, String clave){
        this.estado = estado;
        this.valores = valores;
        this.clave = clave;
    }
}
