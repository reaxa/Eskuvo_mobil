package com.example.eskuvo;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String PREF_KEY = "com.example.eskuvo.PREFERENCE_FILE_KEY";
    private static final int SECRET_KEY = 88;
    private static final int RC_SIGN_IN = 23;

    private EditText userEmailET;
    private EditText userPasswordET;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        createNotificationChannel();

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

        Menu menu = navigationView.getMenu();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateNavigationMenu();

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
                FirebaseUser user = mAuth.getCurrentUser();
                Intent intent = new Intent(this, ProfileActivity.class);
                if (user != null) {
                    intent.putExtra("userName", user.getDisplayName());
                    intent.putExtra("userEmail", user.getEmail());
                }
                startActivity(intent);
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
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Log.i(LOG_TAG, "onCreate");
    }

    private void updateNavigationMenu() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        boolean loggedIn = currentUser != null && !currentUser.isAnonymous();

        menu.findItem(R.id.nav_profile).setVisible(loggedIn);
        menu.findItem(R.id.nav_logout).setVisible(loggedIn);
        menu.findItem(R.id.nav_basket).setVisible(loggedIn);
        menu.findItem(R.id.nav_login).setVisible(!loggedIn);
        menu.findItem(R.id.nav_register).setVisible(!loggedIn);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "ORDER_STATUS_CHANNEL",
                    "Rendelés értesítések",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Értesítések a rendelés állapotáról");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void scheduleOrderNotification(long triggerAtMillis, String message) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("notification_message", message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    private void sendOrderStatusNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "ORDER_STATUS_CHANNEL")
                .setSmallIcon(R.drawable.placeholder)
                .setContentTitle("Esküvői webshop")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(1001, builder.build());
    }

    public void login(View view) {
        String email = userEmailET.getText().toString();
        String password = userPasswordET.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Log.e(LOG_TAG, "Hiányzó email vagy jelszó!");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(LOG_TAG, "Sikeres bejelentkezés");
                        startActivity(new Intent(this, ProfileActivity.class));
                    } else {
                        Log.e(LOG_TAG, "Bejelentkezési hiba", task.getException());
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(LOG_TAG, "Notification permission granted");
            } else {
                Log.i(LOG_TAG, "Notification permission denied");
            }
        }
    }

    public void loginWithGoogle(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void loginAsAnonym(View view) {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(LOG_TAG, "Sikeres anonim bejelentkezés");
                        startActivity(new Intent(this, Aboutus.class));
                    } else {
                        Log.e(LOG_TAG, "Anonim bejelentkezési hiba", task.getException());
                    }
                });
    }

    public void register(View view) {
        Intent intent = new Intent(this, Register.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(LOG_TAG, "Google sign-in failed", e);
            }
        }
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(LOG_TAG, "Sikeres Google bejelentkezés");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // FELHASZNÁLÓI ADATOK MENTÉSE/FRISSÍTÉSE A FIRESTORE-BA
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", user.getDisplayName()); // Google fiók neve
                            userData.put("email", user.getEmail());     // Google fiók emailje
                            // Egyéb adatok, ha vannak, pl. profilkép URL:
                            // if (user.getPhotoUrl() != null) {
                            //     userData.put("profileImageUrl", user.getPhotoUrl().toString());
                            // }

                            // .set(userData, SetOptions.merge()) használata, hogy ha már létezik
                            // a dokumentum más adatokkal (pl. regisztráció után Google login),
                            // akkor csak frissítse/adja hozzá az új mezőket, a régieket ne törölje.
                            db.collection("users").document(user.getUid())
                                    .set(userData, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(LOG_TAG, "Google felhasználó adatai mentve/frissítve a Firestore-ban.");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(LOG_TAG, "Hiba a Google felhasználó adatainak Firestore-ba mentésekor.", e);
                                    });
                        }
                        // Jelentkeztesd be a felhasználót és irányítsd a profil oldalra
                        startActivity(new Intent(this, ProfileActivity.class));
                        finish(); // Bezárja a MainActivity-t, hogy ne tudjon visszalépni a login képernyőre
                    } else {
                        Log.e(LOG_TAG, "Google bejelentkezési hiba", task.getException());
                        Toast.makeText(MainActivity.this, "Google bejelentkezés sikertelen.", Toast.LENGTH_SHORT).show();
                    }
                });
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

    @Override protected void onStart() { super.onStart(); updateNavigationMenu(); Log.i(LOG_TAG, "onStart"); }
    @Override protected void onResume() { super.onResume(); updateNavigationMenu(); Log.i(LOG_TAG, "onResume"); }
    @Override protected void onStop() { super.onStop(); Log.i(LOG_TAG, "onStop"); }
    @Override protected void onRestart() { super.onRestart(); Log.i(LOG_TAG, "onRestart"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.i(LOG_TAG, "onDestroy"); }
}
