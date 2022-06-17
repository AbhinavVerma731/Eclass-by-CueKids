package com.cuekids.eclass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.cuekids.eclass.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    SharedPreferences shp;
    Animation atg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        atg = AnimationUtils.loadAnimation(this, R.anim.atg);
        binding.splashImg.startAnimation(atg);

        new Handler().postDelayed(() -> {
            if (shp == null)
                shp = getSharedPreferences("myPreferences", MODE_PRIVATE);
            String phone = shp.getString("phone", "");
            if (phone != null && !phone.equals("")) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent i = new Intent(SplashActivity.this, WelcomeActivity.class);
                startActivity(i);
                finish();
            }
        }, 3000);
    }
}