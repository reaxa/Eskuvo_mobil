package com.example.eskuvo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class Register extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = Register.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();

    EditText userNameEditText;
    EditText userEmailEditText;
    EditText passwordEditText;
    EditText password2EditText;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);
        if (secret_key != 88) {
            finish();
        }

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

        mAuth = FirebaseAuth.getInstance();

        // Toolbar és navigációs menü beállítása
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
                        Log.d(LOG_TAG, "Sikeres regisztráció");
                        Toast.makeText(Register.this, "Sikeres regisztráció!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Register.this, Aboutus.class));
                        finish();
                    } else {
                        Log.e(LOG_TAG, "Regisztrációs hiba: ", task.getException());
                        Toast.makeText(Register.this, "Regisztrációs hiba: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void login(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("SECRET_KEY", 66);
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
