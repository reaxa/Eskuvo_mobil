package com.example.eskuvo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = Register.class.getName();
    private static final String PREF_KEY = "com.example.eskuvo.PREFERENCE_FILE_KEY";

    EditText userNameEditText;
    EditText userEmailEditText;
    EditText passwordEditText;
    EditText password2EditText;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;   // csak itt deklaráljuk egyszer

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);
        if (secret_key != 88) {
            finish();
        }

        mAuth = FirebaseAuth.getInstance();  // itt inicializáljuk

        userNameEditText = findViewById(R.id.editTextUserName);
        userEmailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        password2EditText = findViewById(R.id.editTextPassword2);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String userName = preferences.getString("userName", "");
        String password = preferences.getString("password", "");

        userNameEditText.setText(userName);
        passwordEditText.setText(password);
        password2EditText.setText(password);

        // Toolbar beállítás
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Menü állapot kezelése, maradjon az eredeti kódod
        Menu menu = navigationView.getMenu();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null && !currentUser.isAnonymous()) {
            menu.findItem(R.id.nav_profile).setVisible(true);
            menu.findItem(R.id.nav_logout).setVisible(true);
            menu.findItem(R.id.nav_basket).setVisible(true);
            menu.findItem(R.id.nav_login).setVisible(false);
            menu.findItem(R.id.nav_register).setVisible(false);
        } else {
            menu.findItem(R.id.nav_profile).setVisible(false);
            menu.findItem(R.id.nav_logout).setVisible(false);
            menu.findItem(R.id.nav_basket).setVisible(false);
            menu.findItem(R.id.nav_login).setVisible(true);
            menu.findItem(R.id.nav_register).setVisible(true);
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_login) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_register) {
                Intent intent = new Intent(this, Register.class);
                intent.putExtra("SECRET_KEY", 88);
                startActivity(intent);
            } else if (id == R.id.nav_about) {
                startActivity(new Intent(this, Aboutus.class));
            } else if (id == R.id.nav_dekoraciok) {
                startActivity(new Intent(this, decorations.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (id == R.id.nav_basket) {
                startActivity(new Intent(this, BasketActivity.class));
            } else if (id == R.id.nav_logout) {
                mAuth.signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        Log.i(LOG_TAG, "onCreate");
    }

    public void register(View view) {
        String userName = userNameEditText.getText().toString().trim();
        String email = userEmailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String password2 = password2EditText.getText().toString().trim();

        if (userName.isEmpty() || email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
            Toast.makeText(this, "Minden mezőt ki kell tölteni!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(password2)) {
            Toast.makeText(this, "A jelszavak nem egyeznek!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(userName)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            // FELHASZNÁLÓI ADATOK MENTÉSE A FIRESTORE-BA
                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("name", userName); // A regisztrációkor megadott név
                                            userData.put("email", email);   // A regisztrációkor megadott email

                                            db.collection("users").document(user.getUid())
                                                    .set(userData) // .set() felülírja, ha már létezne (új regisztrációnál ez jó)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d(LOG_TAG, "Felhasználói adatok sikeresen mentve a Firestore-ba.");
                                                        Toast.makeText(Register.this, "Sikeres regisztráció!", Toast.LENGTH_SHORT).show();
                                                        // Indítsd el a ProfileActivity-t vagy a MainActivity-t a bejelentkezés után
                                                        startActivity(new Intent(Register.this, ProfileActivity.class));
                                                        finish(); // Bezárja a Register activity-t
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.w(LOG_TAG, "Hiba a felhasználói adatok Firestore-ba mentésekor.", e);
                                                        Toast.makeText(Register.this, "Regisztráció sikeres, de a profiladatok mentése nem sikerült: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                        // Ebben az esetben is tovább lehet engedni, de a profiloldalon nem lesz adat
                                                        startActivity(new Intent(Register.this, ProfileActivity.class));
                                                        finish();
                                                    });
                                        } else {
                                            Toast.makeText(Register.this, "Profil frissítési hiba: " + profileTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(Register.this, "Regisztrációs hiba: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    public void login(View view) {
        Intent intent = new Intent(this, MainActivity.class);
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
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userName", userNameEditText.getText().toString());
        editor.putString("password", passwordEditText.getText().toString());
        editor.apply();
        Log.i(LOG_TAG, "onPause");
    }

    // Lifecycle logok
    @Override protected void onStart() { super.onStart(); Log.i(LOG_TAG, "onStart"); }
    @Override protected void onResume() { super.onResume(); Log.i(LOG_TAG, "onResume"); }
    @Override protected void onRestart() { super.onRestart(); Log.i(LOG_TAG, "onRestart"); }
    @Override protected void onStop() { super.onStop(); Log.i(LOG_TAG, "onStop"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.i(LOG_TAG, "onDestroy"); }
}
