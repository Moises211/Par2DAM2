package com.example.reusisam;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import android.content.res.ColorStateList;
import androidx.fragment.app.Fragment;

import com.example.reusisam.data.local.Parcial2Dam2Database;
import com.example.reusisam.data.local.entity.Usuario;
import com.example.reusisam.data.remote.FirebaseCollections;
import com.example.reusisam.data.remote.FirebaseProvider;
import com.example.reusisam.data.remote.model.UsuarioFirebase;
import com.example.reusisam.data.security.PasswordHasher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PerfilFragment extends Fragment {

    private TextView nombreVisible;
    private TextView emailVisible;
    private String emailActual;
    private ExecutorService databaseExecutor;

    public PerfilFragment() {
        // Constructor vacío requerido
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        nombreVisible = view.findViewById(R.id.tvPerfilNombre);
        emailVisible = view.findViewById(R.id.tvPerfilEmail);
        Button modificar = view.findViewById(R.id.btnModificarPerfil);
        databaseExecutor = Executors.newSingleThreadExecutor();

        emailActual = requireContext().getSharedPreferences("UserSession", 0)
                .getString("USER_EMAIL", null);
        if (emailActual != null) {
            emailVisible.setText(emailActual);
            cargarPerfil(emailActual);
            modificar.setOnClickListener(v -> mostrarDialogoEdicion());
        } else {
            emailVisible.setText("No disponible");
            modificar.setEnabled(false);
            Toast.makeText(requireContext(), "Sesión no disponible", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    private void cargarPerfil(String email) {
        FirebaseProvider.firestore().collection(FirebaseCollections.USUARIOS)
                .whereEqualTo("email", email).limit(1).get()
                .addOnSuccessListener(snapshot -> {
                    UsuarioFirebase remoto = snapshot.isEmpty()
                            ? null : snapshot.getDocuments().get(0).toObject(UsuarioFirebase.class);
                    if (remoto != null) {
                        mostrarNombre(remoto.getNombre());
                    }
                })
                .addOnFailureListener(error -> cargarPerfilLocal(email));
    }

    private void cargarPerfilLocal(String email) {
        if (email == null) return;
        databaseExecutor.execute(() -> {
            Usuario local = Parcial2Dam2Database.getInstance(requireContext())
                    .usuarioDao().obtenerPorEmail(email);
            if (local != null && isAdded()) {
                requireActivity().runOnUiThread(() -> mostrarNombre(local.getNombre()));
            }
        });
    }

    private void mostrarNombre(String valor) {
        String nombreActual = valor == null ? "" : valor;
        nombreVisible.setText(nombreActual);
    }

    private void actualizarPerfil(
            String email, EditText nombre, EditText password, EditText confirmPassword) {
        String nuevoNombre = nombre.getText().toString().trim();
        String nuevaPassword = password.getText().toString();
        String confirmacion = confirmPassword.getText().toString();
        if (TextUtils.isEmpty(nuevoNombre)) {
            nombre.setError("El nombre es obligatorio");
            return;
        }
        if (!TextUtils.isEmpty(nuevaPassword)
                && (nuevaPassword.length() < 8
                || !nuevaPassword.matches(".*[A-Za-z].*")
                || !nuevaPassword.matches(".*\\d.*"))) {
            password.setError("Mínimo 8 caracteres, con letras y números");
            return;
        }
        if (!nuevaPassword.equals(confirmacion)) {
            confirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        FirebaseProvider.firestore().collection(FirebaseCollections.USUARIOS)
                .whereEqualTo("email", email).limit(1).get()
                .addOnSuccessListener(unused -> {
                    if (unused.isEmpty()) {
                        guardarPerfilLocal(email, nuevoNombre, nuevaPassword);
                        return;
                    }
                    java.util.Map<String, Object> cambios = new java.util.HashMap<>();
                    cambios.put("nombre", nuevoNombre);
                    if (!TextUtils.isEmpty(nuevaPassword)) {
                        cambios.put("password", PasswordHasher.hash(nuevaPassword));
                    }
                    unused.getDocuments().get(0).getReference()
                            .update(cambios)
                            .addOnSuccessListener(done -> guardarPerfilLocal(email, nuevoNombre, nuevaPassword))
                            .addOnFailureListener(error -> mostrarError(
                                    "No se pudo actualizar el perfil remoto: " + error.getMessage()));
                })
                .addOnFailureListener(error -> mostrarError("No se pudo actualizar el perfil: " + error.getMessage()));
    }

    private void guardarPerfilLocal(String email, String nombreActualizado, String passwordActualizada) {
        databaseExecutor.execute(() -> {
            Parcial2Dam2Database database = Parcial2Dam2Database.getInstance(requireContext());
            Usuario local = database.usuarioDao().obtenerPorEmail(email);
            if (local != null) {
                local.setNombre(nombreActualizado);
                if (!TextUtils.isEmpty(passwordActualizada)) {
                    local.setPassword(PasswordHasher.hash(passwordActualizada));
                }
                database.usuarioDao().actualizar(local);
            }
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    mostrarNombre(nombreActualizado);
                    Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void mostrarError(String mensaje) {
        if (isAdded()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }

    private void mostrarDialogoEdicion() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_perfil, null);
        EditText nombre = dialogView.findViewById(R.id.etPerfilNombre);
        EditText password = dialogView.findViewById(R.id.etPerfilPassword);
        EditText confirmPassword = dialogView.findViewById(R.id.etPerfilConfirmPassword);
        nombre.setText(nombreVisible.getText());

        AlertDialog dialogo = new AlertDialog.Builder(requireContext())
                .setTitle("Modificar perfil")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dlg, which) ->
                        actualizarPerfil(emailActual, nombre, password, confirmPassword))
                .setNegativeButton("Cancelar", null)
                .show();
        configurarDialogo(dialogo);
    }

    private void configurarDialogo(AlertDialog dialog) {
        if (dialog.getWindow() != null) {
            int ancho = (int) (getResources().getDisplayMetrics().widthPixels * 0.92f);
            dialog.getWindow().setLayout(ancho, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        aplicarEstiloBotones(dialog);
    }

    private void aplicarEstiloBotones(AlertDialog dialog) {
        int azul = ContextCompat.getColor(requireContext(), R.color.blue);
        int blanco = ContextCompat.getColor(requireContext(), R.color.white);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(blanco);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setBackgroundTintList(ColorStateList.valueOf(azul));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(azul);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setBackgroundTintList(ColorStateList.valueOf(blanco));
    }
}