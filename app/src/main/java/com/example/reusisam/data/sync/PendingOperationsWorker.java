package com.example.reusisam.data.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.reusisam.data.local.Parcial2Dam2Database;
import com.example.reusisam.data.local.entity.PendingOperation;
import com.example.reusisam.data.remote.FirebaseProvider;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PendingOperationsWorker extends Worker {

    private final Parcial2Dam2Database database;
    private final FirebaseFirestore firestore;
    private final Gson gson = new Gson();

    public PendingOperationsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        database = Parcial2Dam2Database.getInstance(context);
        firestore = FirebaseProvider.firestore();
    }

    @NonNull
    @Override
    public Result doWork() {
        List<PendingOperation> operations = database.pendingOperationDao().obtenerTodas();
        for (PendingOperation operation : operations) {
            try {
                if ("DELETE".equals(operation.getOperation())) {
                    Tasks.await(firestore.collection(operation.getCollection())
                            .document(operation.getDocumentId()).delete());
                } else {
                    Type type = new TypeToken<HashMap<String, Object>>() { }.getType();
                    Map<String, Object> payload = gson.fromJson(operation.getPayload(), type);
                    Tasks.await(firestore.collection(operation.getCollection())
                            .document(operation.getDocumentId()).set(payload));
                }
                database.pendingOperationDao().eliminar(operation);
            } catch (Exception error) {
                return Result.retry();
            }
        }
        return Result.success();
    }
}
