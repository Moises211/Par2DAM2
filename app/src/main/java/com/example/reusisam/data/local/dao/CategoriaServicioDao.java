package com.example.reusisam.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.reusisam.data.local.entity.CategoriaServicio;

import java.util.List;

@Dao
public interface CategoriaServicioDao {

    @Insert
    long insertar(CategoriaServicio categoria);

    @Update
    int actualizar(CategoriaServicio categoria);

    @Delete
    int eliminar(CategoriaServicio categoria);

    @Query("SELECT * FROM categorias_servicio ORDER BY nombre")
    List<CategoriaServicio> obtenerTodas();

    @Query("SELECT * FROM categorias_servicio WHERE id = :id LIMIT 1")
    CategoriaServicio obtenerPorId(long id);

    @Query("SELECT * FROM categorias_servicio WHERE nombre = :nombre LIMIT 1")
    CategoriaServicio obtenerPorNombre(String nombre);

    @Query("UPDATE categorias_servicio SET nombre = :nuevoNombre WHERE nombre = :nombreActual")
    int actualizarNombre(String nombreActual, String nuevoNombre);

    @Query("DELETE FROM categorias_servicio WHERE nombre = :nombre")
    int eliminarPorNombre(String nombre);

    @Query("DELETE FROM categorias_servicio")
    int eliminarTodas();
}
