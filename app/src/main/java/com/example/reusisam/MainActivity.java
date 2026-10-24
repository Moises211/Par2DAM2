package com.example.reusisam;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.reusisam.data.local.Parcial2Dam2Database;
import com.example.reusisam.data.local.entity.Usuario;
import com.example.reusisam.data.remote.FirebaseCollections;
import com.example.reusisam.data.remote.FirebaseProvider;
import com.example.reusisam.data.security.PasswordHasher;
import com.example.reusisam.data.security.CredentialCipher;
import com.example.reusisam.data.remote.model.UsuarioFirebase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btAMExit;
    private Button btnLogin;
    private Toolbar toolbar;
    private CheckBox cbAMPassword;
    private CheckBox cbAMRemember;
    private ExecutorService databaseExecutor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (haySesionActiva()) {
            abrirHome(false);
            return;
        }
        setContentView(R.layout.activity_main);
        databaseExecutor = Executors.newSingleThreadExecutor();

        toolbar = findViewById(R.id.toolbarAR);
        toolbar.setTitle("Login de Usuario");
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        setSupportActionBar(toolbar);

        etEmail = findViewById(R.id.et_AMUser); // Ajusta el ID según tu XML actual
        etPassword = findViewById(R.id.et_AMPassword); // Ajusta el ID según tu XML actual
        btnLogin = findViewById(R.id.bt_AMLogin);

        btAMExit = findViewById(R.id.bt_AMExit);
        cbAMPassword = findViewById(R.id.cb_AMPassword);
        cbAMRemember = findViewById(R.id.cb_AMRemember);
        cargarCredencialesRecordadas();

        btAMExit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Salir();
            }
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                if (!validarCredenciales(email, password)) {
                    return;
                }
                iniciarSesion(email, password);
            } else {
                Toast.makeText(MainActivity.this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        cbAMPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    etPassword.setTransformationMethod(android.text.method.HideReturnsTransformationMethod.getInstance());
                }else{
                    etPassword.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean validarCredenciales(String email, String password) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingresa un correo válido");
            return false;
        }
        if (password.length() < 8 || !password.matches(".*[A-Za-z].*")
                || !password.matches(".*\\d.*")) {
            etPassword.setError("Mínimo 8 caracteres, con letras y números");
            return false;
        }
        return true;
    }

    private void guardarSesion(String email) {
        getSharedPreferences("UserSession", MODE_PRIVATE)
                .edit()
                .putString("USER_EMAIL", email)
                .putBoolean("IS_LOGGED_IN", true)
                .apply();
    }

    private boolean haySesionActiva() {
        android.content.SharedPreferences preferences =
                getSharedPreferences("UserSession", MODE_PRIVATE);
        return preferences.getBoolean("IS_LOGGED_IN", false)
                && !android.text.TextUtils.isEmpty(preferences.getString("USER_EMAIL", null));
    }

    private void cargarCredencialesRecordadas() {
        android.content.SharedPreferences preferences =
                getSharedPreferences("UserSession", MODE_PRIVATE);
        if (!preferences.getBoolean("REMEMBER_CREDENTIALS", false)) {
            return;
        }
        String email = preferences.getString("REMEMBERED_EMAIL", null);
        String encryptedPassword = preferences.getString("REMEMBERED_PASSWORD", null);
        String password = encryptedPassword == null ? null : CredentialCipher.decrypt(encryptedPassword);
        if (email != null && password != null) {
            etEmail.setText(email);
            etPassword.setText(password);
            cbAMRemember.setChecked(true);
        }
    }

    private void guardarCredencialesRecordadas(String email, String password) {
        android.content.SharedPreferences.Editor editor =
                getSharedPreferences("UserSession", MODE_PRIVATE).edit();
        if (cbAMRemember.isChecked()) {
            editor.putBoolean("REMEMBER_CREDENTIALS", true)
                    .putString("REMEMBERED_EMAIL", email)
                    .putString("REMEMBERED_PASSWORD", CredentialCipher.encrypt(password));
        } else {
            editor.remove("REMEMBER_CREDENTIALS")
                    .remove("REMEMBERED_EMAIL")
                    .remove("REMEMBERED_PASSWORD");
        }
        editor.apply();
    }

    private void abrirHome(boolean mostrarMensaje) {
        if (mostrarMensaje) {
            Toast.makeText(this, "¡Login exitoso!", Toast.LENGTH_SHORT).show();
        }
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void iniciarSesion(String email, String password) {
        FirebaseProvider.firestore().collection(FirebaseCollections.USUARIOS)
                .whereEqualTo("email", email).limit(1).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        iniciarSesionOffline(email, password);
                        return;
                    }
                    UsuarioFirebase remoto = snapshot.getDocuments().get(0)
                            .toObject(UsuarioFirebase.class);
                    if (remoto != null && PasswordHasher.hash(password)
                            .equals(remoto.getPassword())) {
                        guardarUsuarioLocal(email, remoto.getNombre(), password);
                    } else {
                        Toast.makeText(this, "Correo o contraseña incorrectos",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(error -> iniciarSesionOffline(email, password));
    }

    private void guardarUsuarioLocal(String email, String nombre, String password) {
        databaseExecutor.execute(() -> {
            Parcial2Dam2Database database =
                    Parcial2Dam2Database.getInstance(getApplicationContext());
            Usuario local = database.usuarioDao().obtenerPorEmail(email);
            if (local == null) {
                database.usuarioDao().insertar(
                        new Usuario(nombre, email, PasswordHasher.hash(password)));
            }
            runOnUiThread(() -> {
                guardarCredencialesRecordadas(email, password);
                guardarSesion(email);
                abrirHome(true);
            });
        });
    }

    private void iniciarSesionOffline(String email, String password) {
        databaseExecutor.execute(() -> {
            Usuario local = Parcial2Dam2Database.getInstance(getApplicationContext())
                    .usuarioDao().obtenerPorEmail(email);
            boolean valido = local != null
                    && local.getPassword().equals(PasswordHasher.hash(password));
            runOnUiThread(() -> {
                if (valido) {
                    guardarCredencialesRecordadas(email, password);
                    guardarSesion(email);
                    abrirHome(true);
                } else {
                    Toast.makeText(this,
                            "No fue posible iniciar sesión. Verifica tus credenciales.",
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int idmenu = item.getItemId();
        if(idmenu == R.id.btnRegistrar_menu){
            AbrirRegistro();
            return true;
        }
        if(idmenu == R.id.btnSalir_menu ) return Salir();

        return super.onOptionsItemSelected(item);
    }

    private void AbrirRegistro() {
        Intent intent = new Intent(this, RegistarActivity.class);
        startActivity(intent);
    }

    public boolean Salir() {
        finishAffinity();
        return true;
    }
}