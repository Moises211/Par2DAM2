package com.example.reusisam;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.widget.TextView;

import com.example.reusisam.data.local.Parcial2Dam2Database;
import com.example.reusisam.data.remote.FirebaseCollections;
import com.example.reusisam.data.remote.FirebaseProvider;

import java.util.concurrent.Executors;

public class InicioFragment extends Fragment {

    public InicioFragment() {
        // Constructor vacío requerido
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_inicio, container, false);
        TextView totalServicios = view.findViewById(R.id.tvTotalServicios);
        TextView serviciosActivos = view.findViewById(R.id.tvServiciosActivos);
        TextView totalCategorias = view.findViewById(R.id.tvTotalCategorias);
        TextView estado = view.findViewById(R.id.tvResumenEstado);

        FirebaseProvider.firestore().collection(FirebaseCollections.SERVICIOS).get()
                .addOnSuccessListener(servicios -> {
                    int activos = 0;
                    for (com.google.firebase.firestore.DocumentSnapshot documento
                            : servicios.getDocuments()) {
                        if (Boolean.TRUE.equals(documento.getBoolean("estado"))) {
                            activos++;
                        }
                    }
                    totalServicios.setText("Servicios registrados: " + servicios.size());
                    serviciosActivos.setText("Servicios activos: " + activos);
                    cargarCategorias(totalCategorias, estado);
                })
                .addOnFailureListener(error -> cargarResumenLocal(
                        totalServicios, serviciosActivos, totalCategorias, estado));
        return view;
    }

    private void cargarCategorias(TextView totalCategorias, TextView estado) {
        FirebaseProvider.firestore().collection(FirebaseCollections.CATEGORIAS_SERVICIO).get()
                .addOnSuccessListener(categorias -> {
                    totalCategorias.setText("Categorías registradas: " + categorias.size());
                    estado.setText("Información actualizada desde Firestore");
                })
                .addOnFailureListener(error -> estado.setText(
                        "Servicios cargados; no se pudieron consultar las categorías"));
    }

    private void cargarResumenLocal(
            TextView totalServicios,
            TextView serviciosActivos,
            TextView totalCategorias,
            TextView estado
    ) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Parcial2Dam2Database database =
                    Parcial2Dam2Database.getInstance(requireContext());
            java.util.List<com.example.reusisam.data.local.entity.Servicio> servicios =
                    database.servicioDao().obtenerTodos();
            int activos = 0;
            for (com.example.reusisam.data.local.entity.Servicio servicio : servicios) {
                if (servicio.isEstado()) activos++;
            }
            int serviciosActivosLocal = activos;
            int categoriasLocales = database.categoriaServicioDao().obtenerTodas().size();
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    totalServicios.setText("Servicios registrados: " + servicios.size());
                    serviciosActivos.setText("Servicios activos: " + serviciosActivosLocal);
                    totalCategorias.setText("Categorías registradas: " + categoriasLocales);
                    estado.setText("Mostrando información local sin conexión");
                });
            }
        });
    }
}