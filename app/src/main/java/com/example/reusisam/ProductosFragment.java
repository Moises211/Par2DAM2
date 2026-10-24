package com.example.reusisam;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import android.content.res.ColorStateList;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reusisam.DTOS.ProductsDTO;
import com.example.reusisam.data.remote.FirebaseCollections;
import com.example.reusisam.data.remote.FirebaseProvider;
import com.example.reusisam.data.local.Parcial2Dam2Database;
import com.example.reusisam.data.local.entity.PendingOperation;
import com.example.reusisam.data.local.entity.Servicio;
import com.example.reusisam.data.local.entity.CategoriaServicio;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Executors;

public class ProductosFragment extends Fragment implements ProductAdapter.OnProductListener {

    private RecyclerView recyclerView;
    private Button btnAgregar;
    private final Gson gson = new Gson();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_productos, container, false);

        recyclerView = view.findViewById(R.id.recyclerProductos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        btnAgregar = view.findViewById(R.id.btnAgregarProducto);
        Button btnCategorias = view.findViewById(R.id.btnCategorias);

        cargarProductos();

        btnAgregar.setOnClickListener(v -> mostrarDialogoFormulario(null));
        btnCategorias.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, new CategoriasFragment())
                .addToBackStack("categorias")
                .commit());

        return view;
    }

    private void cargarProductos() {
        FirebaseProvider.firestore().collection(FirebaseCollections.SERVICIOS).get()
                .addOnSuccessListener(snapshot -> {
                    List<ProductsDTO> lista = new ArrayList<>();
                    snapshot.getDocuments().forEach(document -> {
                        String nombre = document.getString("nombre");
                        Boolean estado = document.getBoolean("estado");
                        ProductsDTO producto = new ProductsDTO(
                                document.getId().hashCode(),
                                nombre == null ? "" : nombre,
                                estado == null || estado);
                        producto.setFirestoreId(document.getId());
                        producto.setDescription(document.getString("descripcion"));
                        Number costo = document.getDouble("costo");
                        producto.setCost(costo == null ? 0f : costo.floatValue());
                        producto.setCategory(document.getString("categoriaServicioId"));
                        lista.add(producto);
                    });
                    recyclerView.setAdapter(new ProductAdapter(lista, ProductosFragment.this));
                })
                .addOnFailureListener(error -> {
                    cargarProductosLocales();
                    Toast.makeText(getContext(), "Mostrando servicios guardados localmente",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarProductosLocales() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Servicio> locales = Parcial2Dam2Database.getInstance(requireContext())
                    .servicioDao().obtenerTodos();
            List<ProductsDTO> lista = new ArrayList<>();
            for (Servicio servicio : locales) {
                ProductsDTO producto = new ProductsDTO((int) servicio.getId(),
                        servicio.getNombre(), servicio.isEstado());
                producto.setDescription(servicio.getDescripcion());
                producto.setCost(servicio.getCosto());
                producto.setCategory(String.valueOf(servicio.getCategoriaServicioId()));
                producto.setFirestoreId(String.valueOf(servicio.getId()));
                lista.add(producto);
            }
            if (isAdded()) {
                requireActivity().runOnUiThread(() ->
                        recyclerView.setAdapter(new ProductAdapter(lista, ProductosFragment.this)));
            }
        });
    }

    private void mostrarDialogoFormulario(@Nullable ProductsDTO productoExistente) {
        FirebaseProvider.firestore().collection(FirebaseCollections.CATEGORIAS_SERVICIO).get()
                .addOnSuccessListener(snapshot -> {
                    List<String> categorias = new ArrayList<>();
                    snapshot.getDocuments().forEach(document -> {
                        String nombre = document.getString("nombre");
                        if (nombre != null && !nombre.trim().isEmpty()) categorias.add(nombre);
                    });
                    mostrarDialogoFormulario(productoExistente, categorias);
                })
                .addOnFailureListener(error -> cargarCategoriasLocalesParaDialogo(productoExistente));
    }

    private void cargarCategoriasLocalesParaDialogo(@Nullable ProductsDTO productoExistente) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            List<com.example.reusisam.data.local.entity.CategoriaServicio> locales =
                    com.example.reusisam.data.local.Parcial2Dam2Database
                            .getInstance(requireContext()).categoriaServicioDao().obtenerTodas();
            List<String> categorias = new ArrayList<>();
            for (com.example.reusisam.data.local.entity.CategoriaServicio categoria : locales) {
                categorias.add(categoria.getNombre());
            }
            if (isAdded()) {
                requireActivity().runOnUiThread(() ->
                        mostrarDialogoFormulario(productoExistente, categorias));
            }
        });
    }

    private void mostrarDialogoFormulario(
            @Nullable ProductsDTO productoExistente, List<String> categorias) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product, null);

        final EditText inputNombre = dialogView.findViewById(R.id.etNombreDialogo);
        final EditText inputDescripcion = dialogView.findViewById(R.id.etDescripcionDialogo);
        final EditText inputCosto = dialogView.findViewById(R.id.etCostoDialogo);
        final Spinner inputCategoria = dialogView.findViewById(R.id.spCategoriaDialogo);
        final CheckBox inputEstado = dialogView.findViewById(R.id.cbEstadoDialogo);

        Collections.sort(categorias, String.CASE_INSENSITIVE_ORDER);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputCategoria.setAdapter(adapter);

        if (productoExistente != null) {
            inputNombre.setText(productoExistente.getName());
            inputDescripcion.setText(productoExistente.getDescription());
            inputCosto.setText(String.valueOf(productoExistente.getCost()));
            int categoriaSeleccionada = categorias.indexOf(productoExistente.getCategory());
            if (categoriaSeleccionada >= 0) inputCategoria.setSelection(categoriaSeleccionada);
            inputEstado.setChecked(productoExistente.isStatus());
        }

        AlertDialog dialogo = builder.setTitle(
                        productoExistente == null ? "Registrar Servicio" : "Actualizar Servicio")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dlg, which) -> {
                    String nombre = inputNombre.getText().toString().trim();
                    String descripcion = inputDescripcion.getText().toString().trim();
                    String categoria = inputCategoria.getSelectedItem() == null
                            ? "" : inputCategoria.getSelectedItem().toString().trim();
                    float costo;
                    try {
                        costo = Float.parseFloat(inputCosto.getText().toString().trim());
                    } catch (NumberFormatException error) {
                        inputCosto.setError("Ingresa un costo válido");
                        return;
                    }
                    if (!nombre.isEmpty() && !descripcion.isEmpty() && !categoria.isEmpty()
                            && costo >= 0) {
                        if (productoExistente == null) {
                            ProductsDTO nuevo = new ProductsDTO(null, nombre, true);
                            nuevo.setDescription(descripcion);
                            nuevo.setCost(costo);
                            nuevo.setCategory(categoria);
                            nuevo.setStatus(inputEstado.isChecked());
                            guardarProducto(nuevo);
                        } else {
                            productoExistente.setName(nombre);
                            productoExistente.setDescription(descripcion);
                            productoExistente.setCost(costo);
                            productoExistente.setCategory(categoria);
                            productoExistente.setStatus(inputEstado.isChecked());
                            actualizarProducto(productoExistente);
                        }
                    } else {
                        Toast.makeText(getContext(),
                                "Completa nombre, descripción, costo y categoría",
                                Toast.LENGTH_SHORT).show();
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

    private void guardarProducto(ProductsDTO producto) {
        String documentId = UUID.randomUUID().toString();
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", producto.getName());
        datos.put("descripcion", producto.getDescription());
        datos.put("costo", producto.getCost());
        datos.put("estado", producto.isStatus());
        datos.put("categoriaServicioId", producto.getCategory());
        guardarServicioLocal(producto, this::cargarProductosLocales);
        guardarOperacionPendiente(documentId, "CREATE", datos);
        FirebaseProvider.firestore().collection(FirebaseCollections.SERVICIOS)
                .document(documentId).set(datos)
                .addOnSuccessListener(unused -> {
                    eliminarOperacionPendiente(documentId);
                    Toast.makeText(getContext(), "Servicio guardado", Toast.LENGTH_SHORT).show();
                    cargarProductosLocales();
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(getContext(),
                            "Guardado local; se sincronizará al recuperar conexión",
                            Toast.LENGTH_LONG).show();
                });
    }

    private void actualizarProducto(ProductsDTO producto) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", producto.getName());
        datos.put("descripcion", producto.getDescription());
        datos.put("costo", producto.getCost());
        datos.put("estado", producto.isStatus());
        datos.put("categoriaServicioId", producto.getCategory());
        guardarServicioLocal(producto, this::cargarProductosLocales);
        guardarOperacionPendiente(producto.getFirestoreId(), "UPDATE", datos);
        FirebaseProvider.firestore().collection(FirebaseCollections.SERVICIOS)
                .document(producto.getFirestoreId()).update(datos)
                .addOnSuccessListener(unused -> {
                    eliminarOperacionPendiente(producto.getFirestoreId());
                    Toast.makeText(getContext(), "Servicio actualizado", Toast.LENGTH_SHORT).show();
                    cargarProductosLocales();
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(getContext(),
                            "Actualización local; se sincronizará al recuperar conexión",
                            Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onEditClick(ProductsDTO product) {
        mostrarDialogoFormulario(product);
    }

    @Override
    public void onDeleteClick(ProductsDTO product) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar servicio")
                .setMessage("¿Deseas eliminar \"" + product.getName() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarServicio(product))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarServicio(ProductsDTO product) {
        eliminarServicioLocal(product.getFirestoreId(), this::cargarProductosLocales);
        guardarOperacionPendiente(product.getFirestoreId(), "DELETE", new HashMap<>());
        FirebaseProvider.firestore().collection(FirebaseCollections.SERVICIOS)
                .document(product.getFirestoreId()).delete()
                .addOnSuccessListener(unused -> {
                    eliminarOperacionPendiente(product.getFirestoreId());
                    Toast.makeText(getContext(), "Servicio eliminado", Toast.LENGTH_SHORT).show();
                    cargarProductosLocales();
                })
                .addOnFailureListener(error -> Toast.makeText(getContext(),
                        "Eliminación pendiente; se sincronizará al recuperar conexión",
                        Toast.LENGTH_LONG).show());
    }

    private void guardarServicioLocal(ProductsDTO producto, Runnable alTerminar) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Parcial2Dam2Database database = Parcial2Dam2Database.getInstance(requireContext());
            CategoriaServicio categoria = database.categoriaServicioDao()
                    .obtenerPorNombre(producto.getCategory());
            if (categoria == null) {
                long categoriaId = database.categoriaServicioDao().insertar(
                        new CategoriaServicio(producto.getCategory()));
                categoria = database.categoriaServicioDao().obtenerPorId(categoriaId);
            }
            Servicio local = new Servicio(producto.getName(), producto.getDescription(),
                    producto.getCost(), producto.isStatus(),
                    categoria.getId());
            long id = obtenerIdLocal(producto.getFirestoreId());
            if (id > 0 && database.servicioDao().obtenerPorId(id) != null) {
                local.setId(id);
                database.servicioDao().actualizar(local);
            } else {
                database.servicioDao().insertar(local);
            }
            if (isAdded()) {
                requireActivity().runOnUiThread(alTerminar);
            }
        });
    }

    private long obtenerIdLocal(String documentId) {
        if (documentId == null) return 0;
        try {
            return Long.parseLong(documentId);
        } catch (NumberFormatException error) {
            return Math.abs(documentId.hashCode());
        }
    }

    private void eliminarServicioLocal(String documentId, Runnable alTerminar) {
        long id = obtenerIdLocal(documentId);
        if (id > 0) {
            Executors.newSingleThreadExecutor().execute(() ->
                    {
                        Parcial2Dam2Database.getInstance(requireContext()).servicioDao()
                                .eliminarPorId(id);
                        if (isAdded()) requireActivity().runOnUiThread(alTerminar);
                    });
        }
    }

    private void guardarOperacionPendiente(String documentId, String operation,
                                           Map<String, Object> payload) {
        Executors.newSingleThreadExecutor().execute(() ->
                Parcial2Dam2Database.getInstance(requireContext()).pendingOperationDao().insertar(
                        new PendingOperation(FirebaseCollections.SERVICIOS, documentId,
                                operation, gson.toJson(payload))));
    }

    private void eliminarOperacionPendiente(String documentId) {
        Executors.newSingleThreadExecutor().execute(() ->
                Parcial2Dam2Database.getInstance(requireContext()).pendingOperationDao()
                        .eliminarPorDocumento(FirebaseCollections.SERVICIOS, documentId));
    }
}