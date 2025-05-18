package com.example.eskuvo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;


public class MainActivity extends AppCompatActivity {
private static final String LOG_TAG = MainActivity.class.getName();
private static final String PREF_KEY = MainActivity.class.getPackage().toString();
private static final int SECRET_KEY = 88;
private static final int RC_SIGN_IN = 23;

    EditText userEmailET;
    EditText userPasswordET;



    private SharedPreferences preferences;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

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

        Menu menu = navigationView.getMenu();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null && !currentUser.isAnonymous()) {
            menu.findItem(R.id.nav_profile).setVisible(true);
            menu.findItem(R.id.nav_logout).setVisible(true);
            menu.findItem(R.id.nav_basket).setVisible(true);   // <-- Kosár menüpont
            menu.findItem(R.id.nav_login).setVisible(false);
            menu.findItem(R.id.nav_register).setVisible(false);
        } else {
            menu.findItem(R.id.nav_profile).setVisible(false);
            menu.findItem(R.id.nav_logout).setVisible(false);
            menu.findItem(R.id.nav_basket).setVisible(false);  // <-- Kosár menüpont eltüntetése anonim vagy nincs login
            menu.findItem(R.id.nav_login).setVisible(true);
            menu.findItem(R.id.nav_register).setVisible(true);
        }


        // Menüelemek kattintás
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
                startActivity(new Intent(this, BasketActivity.class));  // kosár activity indítása
            } else if (id == R.id.nav_logout) {
                mAuth.signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        userEmailET = findViewById(R.id.editTextEmail);
        userPasswordET = findViewById(R.id.editTextPassword);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // ezt a Firebase Console-ból kapod
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Firebase inicializálása
        mAuth = FirebaseAuth.getInstance();

        Log.i(LOG_TAG, "onCreate");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task=  GoogleSignIn.getSignedInAccountFromIntent(data);

        try{
            GoogleSignInAccount account= task.getResult(ApiException.class);
            Log.d(LOG_TAG, "firebaseAuthWithGoogle: " + account.getId());
            firebaseAuthWithGoogle(account.getIdToken());
        }catch (ApiException e){
            Log.w(LOG_TAG, "Hibás bejelentkezés Google fiókkal");

        }


        }

    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Sikeres Google bejelentkezés");
                    startActivity(new Intent(MainActivity.this, Aboutus.class));
                } else {
                    Log.e(LOG_TAG, "Google bejelentkezési hiba: ", task.getException());
                }
            }
        });

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
                    }
                });
    }

    // Google bejelentkezés megvalósítása
    public void loginWithGoogle(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
        Log.d(LOG_TAG, "Navigating to Register screen with SECRET_KEY = 88");
        startActivity(intent);
    }


    private void updateNavigationMenu() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {

            menu.findItem(R.id.nav_profile).setVisible(true);
            menu.findItem(R.id.nav_login).setVisible(false);
            menu.findItem(R.id.nav_register).setVisible(false);
            menu.findItem(R.id.nav_logout).setVisible(true);
        } else {
            menu.findItem(R.id.nav_profile).setVisible(false);
            menu.findItem(R.id.nav_login).setVisible(true);
            menu.findItem(R.id.nav_register).setVisible(true);
            menu.findItem(R.id.nav_logout).setVisible(false);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationMenu();
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
        updateNavigationMenu();
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