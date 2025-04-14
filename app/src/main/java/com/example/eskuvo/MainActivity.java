package com.example.eskuvo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;



public class MainActivity extends AppCompatActivity implements navos {
private static final String LOG_TAG = MainActivity.class.getName();
private static final String PREF_KEY = MainActivity.class.getPackage().toString();
private static final int SECRET_KEY = 88;

    EditText userEmailET;
    EditText userPasswordET;



    private SharedPreferences preferences;

    private FirebaseAuth mAuth;
    private DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                // Action for about
            } else if (item.getItemId() == R.id.nav_dekoraciok) {
                startActivity(new Intent(this, decorations.class));
            }
            drawerLayout.closeDrawers();
            return true;
        });

        userEmailET = findViewById(R.id.editTextEmail);
        userPasswordET = findViewById(R.id.editTextPassword);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

        // Firebase inicializálása
        mAuth = FirebaseAuth.getInstance();

        Log.i(LOG_TAG, "onCreate");
    }

    public void login(View view) {
        String email = userEmailET.getText().toString();
        String password = userPasswordET.getText().toString();

        // Ellenőrzés, hogy nem üresek-e a mezők
        if (email.isEmpty() || password.isEmpty()) {
            Log.e(LOG_TAG, "Hiányzó email vagy jelszó!");
            return;
        }

        // Firebase bejelentkezés
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(LOG_TAG, "Sikeres bejelentkezés");
                        startActivity(new Intent(MainActivity.this, Aboutus.class));
                    } else {
                        Log.e(LOG_TAG, "Bejelentkezési hiba: ", task.getException());
                        // Itt jelezheted a felhasználónak a hibát, pl. Toast üzenettel
                    }
                });
    }

    // Google bejelentkezés megvalósítása
    public void loginWithGoogle(View view) {
        // Google bejelentkezés kódja ide kerülne
        // Egyszerűsítésként most csak átirányítunk az Aboutus oldalra
        startActivity(new Intent(MainActivity.this, Aboutus.class));
    }

    // Anonim bejelentkezés megvalósítása
    public void loginAsAnonym(View view) {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(LOG_TAG, "Sikeres anonim bejelentkezés");
                        startActivity(new Intent(MainActivity.this, Aboutus.class));
                    } else {
                        Log.e(LOG_TAG, "Anonim bejelentkezési hiba: ", task.getException());
                    }
                });
    }


    public void register(View view) {
        Intent intent = new Intent(this, Register.class);
        intent.putExtra("SECRET_KEY", 88);
        startActivity(intent);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_about) {
            startActivity(new Intent(this, Aboutus.class));
        } else if (id == R.id.nav_dekoraciok) {
            startActivity(new Intent(this, decorations.class));
        } else if (id == R.id.nav_login) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.nav_register) {
            startActivity(new Intent(this, Register.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userName", userEmailET.getText().toString());
        editor.putString("password", userPasswordET.getText().toString());
        editor.apply();
        Log.i(LOG_TAG, "onPause");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");

    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");
    }

}