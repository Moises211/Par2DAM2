package com.example.reusisam;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.reusisam.data.sync.SyncScheduler;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        SyncScheduler.programar(getApplicationContext());

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        toolbar = findViewById(R.id.toolbarAR);
        toolbar.setTitle("Bienvenido");
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            cargarFragment(new InicioFragment());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragmentSeleccionado = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_inicio) {
                fragmentSeleccionado = new InicioFragment();
            } else if (itemId == R.id.nav_productos) {
                fragmentSeleccionado = new ProductosFragment();
            } else if (itemId == R.id.nav_mapa) {
                fragmentSeleccionado = new MapaFragment();
            } else if (itemId == R.id.nav_perfil) {
                fragmentSeleccionado = new PerfilFragment();
            }

            if (fragmentSeleccionado != null) {
                cargarFragment(fragmentSeleccionado);
                return true;
            }

            return false;
        });
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int idmenu = item.getItemId();

        if(idmenu == R.id.nav_cerrarSeccion){
            getSharedPreferences("UserSession", MODE_PRIVATE)
                    .edit()
                    .remove("IS_LOGGED_IN")
                    .remove("USER_EMAIL")
                    .apply();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        if(idmenu == R.id.nav_salir){
            finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .commit();
    }
}