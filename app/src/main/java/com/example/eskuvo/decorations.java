package com.example.eskuvo;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.eskuvo.MainActivity;
import com.example.eskuvo.R;
import com.example.eskuvo.decorations;
import com.google.android.material.navigation.NavigationView;
public class decorations extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutus);

        // Toolbar beállítás
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Hamburger ikon működtetése
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Menüelemek kezelése
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_login) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (item.getItemId() == R.id.nav_register) {
                startActivity(new Intent(this, Register.class));
            } else if (item.getItemId() == R.id.nav_about) {
                startActivity(new Intent(this, Aboutus.class));
            } else if (item.getItemId() == R.id.nav_dekoraciok) {

            }
            drawerLayout.closeDrawers();
            return true;
        });

    }
}
