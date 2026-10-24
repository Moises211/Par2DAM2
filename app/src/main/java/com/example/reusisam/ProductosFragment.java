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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reusisam.DTOS.ProductsDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductosFragment extends Fragment implements ProductAdapter.OnProductListener {

    private RecyclerView recyclerView;
    private Button btnAgregar;
    private ApiService apiService;
    private String tokenJwt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_productos, container, false);

        recyclerView = view.findViewById(R.id.recyclerProductos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        btnAgregar = view.findViewById(R.id.btnAgregarProducto);

        // Obtener Token guardado
        SharedPreferences preferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String rawToken = preferences.getString("JWT_TOKEN", null);

        if (rawToken == null || rawToken.isEmpty()) {
            Toast.makeText(getContext(), "Sesión expirada o token no encontrado. Inicia sesión de nuevo.", Toast.LENGTH_LONG).show();
            // Opcional: Redirigir al Login si no hay token
            return view;
        }

        tokenJwt = "Bearer " + rawToken;
        Log.d("TOKEN_VERIFICATION", "Token final enviado: " + tokenJwt);

        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8085/api/v1/demoapirestdam235/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        cargarProductos();

        btnAgregar.setOnClickListener(v -> mostrarDialogoFormulario(null));

        return view;
    }

    private void cargarProductos() {
        apiService.getProducts(tokenJwt).enqueue(new Callback<List<ProductsDTO>>() {
            @Override
            public void onResponse(Call<List<ProductsDTO>> call, Response<List<ProductsDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProductsDTO> lista = response.body();
                    ProductAdapter adapter = new ProductAdapter(lista, ProductosFragment.this);
                    recyclerView.setAdapter(adapter);
                } else {
                    // AQUÍ IMPRIMIMOS EL CÓDIGO DE ERROR REAL EN LA CONSOLA
                    Log.e("DEBUG_API", "Error código: " + response.code() + " Mensaje: " + response.message());
                    Toast.makeText(getContext(), "Error código: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ProductsDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDialogoFormulario(@Nullable ProductsDTO productoExistente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product, null);

        final EditText inputNombre = dialogView.findViewById(R.id.etNombreDialogo);

        if (productoExistente != null) {
            inputNombre.setText(productoExistente.getName());
        }

        builder.setTitle(productoExistente == null ? "Registrar Producto" : "Actualizar Producto")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = inputNombre.getText().toString().trim();
                    if (!nombre.isEmpty()) {
                        if (productoExistente == null) {
                            // Guardar Nuevo (@POST)
                            ProductsDTO nuevo = new ProductsDTO(null, nombre, true);
                            guardarProducto(nuevo);
                        } else {
                            // Actualizar (@PUT)
                            productoExistente.setName(nombre);
                            actualizarProducto(productoExistente);
                        }
                    } else {
                        Toast.makeText(getContext(), "El nombre no puede ir vacío", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarProducto(ProductsDTO producto) {
        apiService.saveProduct(tokenJwt, producto).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Producto guardado con éxito", Toast.LENGTH_SHORT).show();
                    cargarProductos();
                }
            }
            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarProducto(ProductsDTO producto) {
        apiService.updateProduct(tokenJwt, producto).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Producto actualizado", Toast.LENGTH_SHORT).show();
                    cargarProductos();
                }
            }
            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditClick(ProductsDTO product) {
        mostrarDialogoFormulario(product);
    }

    @Override
    public void onDeleteClick(ProductsDTO product) {
        Map<String, Integer> body = new HashMap<>();
        body.put("id", product.getCode()); // Asumiendo que 'code' es el ID numérico

        apiService.deleteProduct(tokenJwt, body).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Producto eliminado", Toast.LENGTH_SHORT).show();
                    cargarProductos();
                }
            }
            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(getContext(), "Error al eliminar: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}