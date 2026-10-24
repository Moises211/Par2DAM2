package com.example.reusisam.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.reusisam.data.local.entity.PendingOperation;

import java.util.List;

@Dao
public interface PendingOperationDao {

    @Insert
    long insertar(PendingOperation operation);

    @Delete
    int eliminar(PendingOperation operation);

    @Query("SELECT * FROM operaciones_pendientes ORDER BY id")
    List<PendingOperation> obtenerTodas();

    @Query("DELETE FROM operaciones_pendientes WHERE collection = :collection AND documentId = :documentId")
    int eliminarPorDocumento(String collection, String documentId);

    @Query("DELETE FROM operaciones_pendientes")
    int eliminarTodas();
}
