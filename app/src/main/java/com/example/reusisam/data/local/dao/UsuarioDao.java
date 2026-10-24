package com.example.reusisam.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.reusisam.data.local.entity.Usuario;

import java.util.List;

@Dao
public interface UsuarioDao {

    @Insert
    long insertar(Usuario usuario);

    @Update
    int actualizar(Usuario usuario);

    @Delete
    int eliminar(Usuario usuario);

    @Query("SELECT * FROM usuarios ORDER BY nombre")
    List<Usuario> obtenerTodos();

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    Usuario obtenerPorEmail(String email);

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    Usuario obtenerPorId(long id);

    @Query("DELETE FROM usuarios")
    int eliminarTodos();
}
