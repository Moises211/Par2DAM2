package com.example.reusisam.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.reusisam.data.local.entity.Servicio;

import java.util.List;

@Dao
public interface ServicioDao {

    @Insert
    long insertar(Servicio servicio);

    @Update
    int actualizar(Servicio servicio);

    @Delete
    int eliminar(Servicio servicio);

    @Query("SELECT * FROM servicios ORDER BY nombre")
    List<Servicio> obtenerTodos();

    @Query("SELECT * FROM servicios WHERE id = :id LIMIT 1")
    Servicio obtenerPorId(long id);

    @Query("DELETE FROM servicios WHERE id = :id")
    int eliminarPorId(long id);

    @Query("SELECT * FROM servicios WHERE categoriaServicioId = :categoriaId ORDER BY nombre")
    List<Servicio> obtenerPorCategoria(long categoriaId);

    @Query("DELETE FROM servicios")
    int eliminarTodos();
}
