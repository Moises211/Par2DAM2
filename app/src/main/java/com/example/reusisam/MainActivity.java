package com.example.reusisam;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.reusisam.DTOS.LoginDTO; // Asegúrate de crear este DTO con email y password
import com.example.reusisam.DTOS.TokenDTO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btAMExit;
    private Button btnLogin;
    private Toolbar toolbar;
    private CheckBox cbAMPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbarAR);
        toolbar.setTitle("Login de Usuario");
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        setSupportActionBar(toolbar);

        etEmail = findViewById(R.id.et_AMUser); // Ajusta el ID según tu XML actual
        etPassword = findViewById(R.id.et_AMPassword); // Ajusta el ID según tu XML actual
        btnLogin = findViewById(R.id.bt_AMLogin);

        btAMExit = findViewById(R.id.bt_AMExit);
        cbAMPassword = findViewById(R.id.cb_AMPassword);

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
                facerLoginEnApi(email, password);
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

    private void facerLoginEnApi(String email, String password) {
        // Configuración de Retrofit (Apunta a tu PC con 10.0.2.2 si usas emulador)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8085/api/v1/demoapirestdam235/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        LoginDTO loginDTO = new LoginDTO(email, password);
        System.out.println("Email and Pass " +  email+ " " + password);
        Call<TokenDTO> call = apiService.login(loginDTO);
        call.enqueue(new Callback<TokenDTO>() {
            @Override
            public void onResponse(Call<TokenDTO> call, Response<TokenDTO> response) {
                System.out.println("respuesta " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    String tokenJwt = response.body().getToken();

                    // AQUÍ SÍ USAMOS SharedPreferences: Guardamos el token para usarlo después
                    SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("JWT_TOKEN", tokenJwt);
                    editor.apply();

                    Toast.makeText(MainActivity.this, "¡Login exitoso!", Toast.LENGTH_SHORT).show();

                    // Pasamos a la pantalla principal (HomeActivity)
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TokenDTO> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
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