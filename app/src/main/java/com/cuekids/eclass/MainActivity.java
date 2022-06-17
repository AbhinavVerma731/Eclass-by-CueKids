package com.cuekids.eclass;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView btn;

    final Fragment f1 = new homeFragment();
    final Fragment f2 = new eventsFragment();
    final Fragment f3 = new coursesFragment();
    final Fragment f4 = new funCornerFragment();
    final Fragment f5 = new profileFragment();

    final FragmentManager fm = getSupportFragmentManager();
    Fragment active = f1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.bottomNavigation);

        fm.beginTransaction().add(R.id.layout_container, f5, "5").hide(f5).commit();
        fm.beginTransaction().add(R.id.layout_container, f4, "4").hide(f4).commit();
        fm.beginTransaction().add(R.id.layout_container, f3, "3").hide(f3).commit();
        fm.beginTransaction().add(R.id.layout_container, f2, "2").hide(f2).commit();
        fm.beginTransaction().add(R.id.layout_container, f1, "1").commit();
        btn.setOnNavigationItemSelectedListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_home:
                fm.beginTransaction().hide(active).show(f1).commit();
                active = f1;
                return true;

            case R.id.menu_events:
                fm.beginTransaction().hide(active).show(f2).commit();
                active = f2;
                return true;

            case R.id.menu_courses:
                fm.beginTransaction().hide(active).show(f3).commit();
                active = f3;
                return true;

            case R.id.menu_funCorner:
                fm.beginTransaction().hide(active).show(f4).commit();
                active = f4;
                return true;

            case R.id.menu_profile:
                fm.beginTransaction().hide(active).show(f5).commit();
                active = f5;
                return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        if (btn.getSelectedItemId() == R.id.menu_home) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Are you sure");
            builder.setMessage("You want to exit ?");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", (dialog, which) -> {
                super.onBackPressed();
                finish();
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            btn.setSelectedItemId(R.id.menu_home);
        }
    }
}