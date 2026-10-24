package com.example.reusisam.data.remote;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.reusisam.data.local.Parcial2Dam2Database;
import com.example.reusisam.data.local.entity.PendingOperation;
import com.example.reusisam.data.local.entity.Servicio;
import com.example.reusisam.data.remote.model.ServicioFirebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CRUD de servicios con caché Room. Firestore mantiene sus propias escrituras
 * offline; la cola local cubre las operaciones que deban reintentarse.
 */
public class ServicioRepository {

    public interface Callback<T> {
        void onSuccess(T value);
        void onError(Exception error);
    }

    private final Parcial2Dam2Database database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    public ServicioRepository(Context context) {
        database = Parcial2Dam2Database.getInstance(context);
    }

    public void obtenerTodos(Callback<List<Servicio>> callback) {
        FirebaseProvider.firestore().collection(FirebaseCollections.SERVICIOS).get()
                .addOnSuccessListener(snapshot -> {
                    List<Servicio> servicios = convertir(snapshot);
                    executor.execute(() -> {
                        database.servicioDao().eliminarTodos();
                        for (Servicio servicio : servicios) database.servicioDao().insertar(servicio);
                        callback.onSuccess(servicios);
                    });
                })
                .addOnFailureListener(error -> executor.execute(() ->
                        callback.onSuccess(database.servicioDao().obtenerTodos())));
    }

    public void crear(String documentId, ServicioFirebase servicio, Callback<Void> callback) {
        FirebaseProvider.firestore().collection(FirebaseCollections.SERVICIOS)
                .document(documentId).set(servicio)
                .addOnSuccessListener(unused -> {
                    guardarLocal(documentId, servicio);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(guardarPendiente(documentId, "CREATE", servicio, callback));
    }

    public void actualizar(String documentId, ServicioFirebase servicio, Callback<Void> callback) {
        FirebaseProvider.firestore().collection(FirebaseCollections.SERVICIOS)
                .document(documentId).set(servicio)
                .addOnSuccessListener(unused -> {
                    guardarLocal(documentId, servicio);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(guardarPendiente(documentId, "UPDATE", servicio, callback));
    }

    public void eliminar(String documentId, Callback<Void> callback) {
        FirebaseProvider.firestore().collection(FirebaseCollections.SERVICIOS)
                .document(documentId).delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(error -> {
                    executor.execute(() -> database.pendingOperationDao().insertar(
                            new PendingOperation(FirebaseCollections.SERVICIOS, documentId,
                                    "DELETE", "")));
                    callback.onError(error);
                });
    }

    private OnFailureListener guardarPendiente(
            String documentId,
            String operation,
            ServicioFirebase servicio,
            Callback<Void> callback
    ) {
        return error -> {
            executor.execute(() -> database.pendingOperationDao().insertar(
                    new PendingOperation(FirebaseCollections.SERVICIOS, documentId,
                            operation, gson.toJson(servicio))));
            callback.onError(error);
        };
    }

    private void guardarLocal(String documentId, ServicioFirebase remoto) {
        executor.execute(() -> {
            Servicio local = new Servicio(remoto.getNombre(), remoto.getDescripcion(),
                    remoto.getCosto(), remoto.isEstado(), parseId(documentId));
            local.setId(parseId(documentId));
            database.servicioDao().insertar(local);
        });
    }

    private List<Servicio> convertir(QuerySnapshot snapshot) {
        List<Servicio> result = new ArrayList<>();
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            ServicioFirebase remoto = document.toObject(ServicioFirebase.class);
            if (remoto != null) {
                Servicio local = new Servicio(remoto.getNombre(), remoto.getDescripcion(),
                        remoto.getCosto(), remoto.isEstado(),
                        parseId(remoto.getCategoriaServicioId()));
                local.setId(parseId(document.getId()));
                result.add(local);
            }
        }
        return result;
    }

    private long parseId(String value) {
        if (value == null) return 0;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return Math.abs(value.hashCode());
        }
    }
}
