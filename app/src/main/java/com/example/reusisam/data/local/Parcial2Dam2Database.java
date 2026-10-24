package com.example.reusisam.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.reusisam.data.local.dao.CategoriaServicioDao;
import com.example.reusisam.data.local.dao.ServicioDao;
import com.example.reusisam.data.local.dao.UsuarioDao;
import com.example.reusisam.data.local.dao.PendingOperationDao;
import com.example.reusisam.data.local.entity.CategoriaServicio;
import com.example.reusisam.data.local.entity.Servicio;
import com.example.reusisam.data.local.entity.Usuario;
import com.example.reusisam.data.local.entity.PendingOperation;

@Database(
        entities = {
                Usuario.class,
                CategoriaServicio.class,
                Servicio.class,
                PendingOperation.class
        },
        version = 2,
        exportSchema = false
)
public abstract class Parcial2Dam2Database extends RoomDatabase {

    private static volatile Parcial2Dam2Database INSTANCE;

    public abstract UsuarioDao usuarioDao();

    public abstract CategoriaServicioDao categoriaServicioDao();

    public abstract ServicioDao servicioDao();

    public abstract PendingOperationDao pendingOperationDao();

    public static Parcial2Dam2Database getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Parcial2Dam2Database.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    Parcial2Dam2Database.class,
                                    "parcial2dam2.db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
