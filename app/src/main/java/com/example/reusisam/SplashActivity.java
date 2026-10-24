package com.example.reusisam;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2500;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable irAlLogin = () -> {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View contenido = findViewById(R.id.iv_SPLogo);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(900);
        contenido.startAnimation(fadeIn);

        handler.postDelayed(irAlLogin, SPLASH_DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(irAlLogin);
        super.onDestroy();
    }
}
