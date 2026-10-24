package com.example.reusisam;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reusisam.data.local.Parcial2Dam2Database;
import com.example.reusisam.data.local.entity.CategoriaServicio;
import com.example.reusisam.data.local.entity.PendingOperation;
import com.example.reusisam.data.remote.FirebaseCollections;
import com.example.reusisam.data.remote.FirebaseProvider;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.UUID;

public class CategoriasFragment extends Fragment implements CategoriaAdapter.Listener {

    private RecyclerView recyclerView;
    private final List<CategoriaDTO> categorias = new ArrayList<>();
    private final Gson gson = new Gson();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categorias, container, false);
        recyclerView = view.findViewById(R.id.recyclerCategorias);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        view.findViewById(R.id.btnVolverServicios).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());
        view.findViewById(R.id.btnAgregarCategoria).setOnClickListener(v ->
                mostrarDialogo(null));
        cargarCategorias();
        return view;
    }

    private void cargarCategorias() {
        FirebaseProvider.firestore().collection(FirebaseCollections.CATEGORIAS_SERVICIO).get()
                .addOnSuccessListener(snapshot -> {
                    categorias.clear();
                    snapshot.getDocuments().forEach(document -> categorias.add(
                            new CategoriaDTO(document.getId(), document.getString("nombre"))));
                    mostrarCategorias();
                    guardarCategoriasLocal();
                })
                .addOnFailureListener(error -> {
                    cargarCategoriasLocal();
                    Toast.makeText(requireContext(), "Mostrando categorías locales",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarCategorias() {
        recyclerView.setAdapter(new CategoriaAdapter(categorias, this));
    }

    private void guardarCategoriasLocal() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Parcial2Dam2Database database = Parcial2Dam2Database.getInstance(requireContext());
            database.categoriaServicioDao().eliminarTodas();
            for (CategoriaDTO categoria : categorias) {
                database.categoriaServicioDao().insertar(new CategoriaServicio(categoria.getNombre()));
            }
        });
    }

    private void cargarCategoriasLocal() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<CategoriaServicio> locales = Parcial2Dam2Database.getInstance(requireContext())
                    .categoriaServicioDao().obtenerTodas();
            categorias.clear();
            for (CategoriaServicio categoria : locales) {
                categorias.add(new CategoriaDTO(null, categoria.getNombre()));
            }
            if (isAdded()) requireActivity().runOnUiThread(this::mostrarCategorias);
        });
    }

    private void mostrarDialogo(@Nullable CategoriaDTO existente) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_categoria, null);
        EditText nombre = dialogView.findViewById(R.id.etNombreCategoria);
        if (existente != null) nombre.setText(existente.getNombre());

        AlertDialog dialogo = new AlertDialog.Builder(requireContext())
                .setTitle(existente == null ? "Nueva categoría" : "Editar categoría")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String valor = nombre.getText().toString().trim();
                    if (valor.isEmpty()) {
                        nombre.setError("El nombre es obligatorio");
                    } else if (existente == null) {
                        guardarCategoria(valor);
                    } else {
                        actualizarCategoria(existente, valor);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
        if (dialogo.getWindow() != null) {
            int ancho = (int) (getResources().getDisplayMetrics().widthPixels * 0.92f);
            dialogo.getWindow().setLayout(ancho, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int azul = ContextCompat.getColor(requireContext(), R.color.blue);
        int blanco = ContextCompat.getColor(requireContext(), R.color.white);
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(blanco);
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setBackgroundTintList(ColorStateList.valueOf(azul));
        dialogo.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(azul);
        dialogo.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setBackgroundTintList(ColorStateList.valueOf(blanco));
    }

    private void guardarCategoria(String nombre) {
        String documentId = UUID.randomUUID().toString();
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombre);
        guardarCategoriaLocal(nombre, documentId, datos, this::cargarCategoriasLocal);
        FirebaseProvider.firestore().collection(FirebaseCollections.CATEGORIAS_SERVICIO)
                .document(documentId).set(datos)
                .addOnSuccessListener(unused -> {
                    eliminarOperacionPendiente(documentId);
                    Toast.makeText(requireContext(), "Categoría guardada", Toast.LENGTH_SHORT).show();
                    cargarCategoriasLocal();
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(requireContext(),
                            "Guardada localmente; se sincronizará al recuperar conexión",
                            Toast.LENGTH_LONG).show();
                    cargarCategoriasLocal();
                });
    }

    private void guardarCategoriaLocal(String nombre, String documentId,
                                       Map<String, Object> datos, Runnable alTerminar) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Parcial2Dam2Database database = Parcial2Dam2Database.getInstance(requireContext());
            database.categoriaServicioDao().insertar(new CategoriaServicio(nombre));
            database.pendingOperationDao().insertar(new PendingOperation(
                    FirebaseCollections.CATEGORIAS_SERVICIO, documentId, "CREATE",
                    gson.toJson(datos)));
            if (isAdded()) requireActivity().runOnUiThread(alTerminar);
        });
    }

    private void actualizarCategoria(CategoriaDTO categoria, String nombre) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Parcial2Dam2Database.getInstance(requireContext()).categoriaServicioDao()
                    .actualizarNombre(categoria.getNombre(), nombre);
            if (isAdded()) requireActivity().runOnUiThread(this::cargarCategoriasLocal);
        });
        if (categoria.getFirestoreId() == null) {
            Toast.makeText(requireContext(), "Categoría actualizada localmente",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombre);
        guardarOperacionPendiente(categoria.getFirestoreId(), "UPDATE", datos);
        FirebaseProvider.firestore().collection(FirebaseCollections.CATEGORIAS_SERVICIO)
                .document(categoria.getFirestoreId()).update("nombre", nombre)
                .addOnSuccessListener(unused -> {
                    eliminarOperacionPendiente(categoria.getFirestoreId());
                    Toast.makeText(requireContext(), "Categoría actualizada", Toast.LENGTH_SHORT).show();
                    cargarCategoriasLocal();
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(requireContext(),
                            "Actualización local; se sincronizará al recuperar conexión",
                            Toast.LENGTH_LONG).show();
                    cargarCategoriasLocal();
                });
    }

    @Override
    public void onEdit(CategoriaDTO categoria) {
        mostrarDialogo(categoria);
    }

    @Override
    public void onDelete(CategoriaDTO categoria) {
        if (categoria.getFirestoreId() == null) {
            eliminarCategoriaLocal(categoria, this::cargarCategoriasLocal);
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar categoría")
                .setMessage("¿Deseas eliminar \"" + categoria.getNombre() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarCategoriaLocal(categoria, this::cargarCategoriasLocal);
                    guardarOperacionPendiente(categoria.getFirestoreId(), "DELETE", new HashMap<>());
                    FirebaseProvider.firestore()
                                .collection(FirebaseCollections.CATEGORIAS_SERVICIO)
                                .document(categoria.getFirestoreId()).delete()
                                .addOnSuccessListener(unused -> {
                                    eliminarOperacionPendiente(categoria.getFirestoreId());
                                    cargarCategoriasLocal();
                                })
                                .addOnFailureListener(error ->
                                        Toast.makeText(requireContext(),
                                                "Eliminación local; se sincronizará al recuperar conexión",
                                                Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarCategoriaLocal(CategoriaDTO categoria, Runnable alTerminar) {
        Executors.newSingleThreadExecutor().execute(() ->
                {
                    Parcial2Dam2Database.getInstance(requireContext()).categoriaServicioDao()
                            .eliminarPorNombre(categoria.getNombre());
                    if (isAdded()) requireActivity().runOnUiThread(alTerminar);
                });
        Toast.makeText(requireContext(), "Categoría eliminada localmente", Toast.LENGTH_SHORT).show();
    }

    private void guardarOperacionPendiente(String documentId, String operation,
                                           Map<String, Object> payload) {
        Executors.newSingleThreadExecutor().execute(() ->
                Parcial2Dam2Database.getInstance(requireContext()).pendingOperationDao().insertar(
                        new PendingOperation(FirebaseCollections.CATEGORIAS_SERVICIO, documentId,
                                operation, gson.toJson(payload))));
    }

    private void eliminarOperacionPendiente(String documentId) {
        Executors.newSingleThreadExecutor().execute(() ->
                Parcial2Dam2Database.getInstance(requireContext()).pendingOperationDao()
                        .eliminarPorDocumento(FirebaseCollections.CATEGORIAS_SERVICIO, documentId));
    }

    private void mostrarError(String mensaje) {
        if (isAdded()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }
}
