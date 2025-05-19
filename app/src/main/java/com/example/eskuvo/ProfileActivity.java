package com.example.eskuvo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

import com.example.eskuvo.model.Order;
import com.example.eskuvo.adapter.OrderAdapter;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private TextView tvName;
    private TextView tvEmail;
    private Button changeEmailButton;
    private Button deleteProfileButton;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.tool_bar_profile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        drawerLayout = findViewById(R.id.drawer_layout_profile);
        navigationView = findViewById(R.id.nav_view_profile);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        changeEmailButton = findViewById(R.id.change_email_button);
        deleteProfileButton = findViewById(R.id.delete_profile_button);
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);

        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList);
        ordersRecyclerView.setAdapter(orderAdapter);

        Menu menu = navigationView.getMenu();
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
                drawerLayout.closeDrawer(GravityCompat.START);
            } else if (id == R.id.nav_basket) {
                startActivity(new Intent(this, BasketActivity.class));
            } else if (id == R.id.nav_logout) {
                mAuth.signOut();
                Toast.makeText(ProfileActivity.this, "Sikeresen kijelentkezett!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        if (currentUser != null && !currentUser.isAnonymous()) {
            tvEmail.setText(getString(R.string.email_format, currentUser.getEmail()));

            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userName = documentSnapshot.getString("name");
                            if (userName != null) {
                                tvName.setText(getString(R.string.neved_format, userName));
                            } else {
                                tvName.setText(getString(R.string.nevednem));
                            }
                        } else {
                            tvName.setText(getString(R.string.nevednem));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileActivity", "Hiba a felhasználó nevének lekérdezésekor", e);
                        tvName.setText(getString(R.string.nevednem));
                    });

            changeEmailButton.setVisibility(View.VISIBLE);
            deleteProfileButton.setVisibility(View.VISIBLE);

            changeEmailButton.setOnClickListener(v -> handleChangeEmail());
            deleteProfileButton.setOnClickListener(v -> handleDeleteProfile());

            loadOrders();

        } else {
            tvName.setText(getString(R.string.nevednem));
            tvEmail.setText(getString(R.string.email_format, "Nincs bejelentkezve"));
            changeEmailButton.setVisibility(View.GONE);
            deleteProfileButton.setVisibility(View.GONE);
            Toast.makeText(this, "Jelentkezzen be a profilfunkciók eléréséhez.", Toast.LENGTH_LONG).show();
        }
    }

    private void handleChangeEmail() {
        if (currentUser == null) {
            Toast.makeText(this, "Nincs bejelentkezve felhasználó.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email cím módosítása");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText newEmailInput = new EditText(this);
        newEmailInput.setHint("Új email cím");
        newEmailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(newEmailInput);

        final EditText passwordInput = new EditText(this);
        passwordInput.setHint("Jelenlegi jelszó");
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordInput);

        builder.setView(layout);

        builder.setPositiveButton("Módosít", (dialog, which) -> {
            String newEmail = newEmailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (newEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(ProfileActivity.this, "Kérem töltse ki az összes mezőt.", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            currentUser.updateEmail(newEmail)
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            Toast.makeText(ProfileActivity.this, "Email cím sikeresen módosítva!", Toast.LENGTH_LONG).show();
                                            tvEmail.setText(getString(R.string.email_format, currentUser.getEmail()));
                                        } else {
                                            String errorMessage = "Hiba az email cím módosításakor.";
                                            if (emailTask.getException() != null) {
                                                errorMessage += " " + emailTask.getException().getMessage();
                                                Log.e("ProfileActivity", "Email módosítás hiba", emailTask.getException());
                                            }
                                            Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            String errorMessage = "Jelszó érvénytelen, vagy hiba a hitelesítéskor.";
                            if (task.getException() != null) {
                                errorMessage += " " + task.getException().getMessage();
                                Log.e("ProfileActivity", "Újra hitelesítés hiba (email módosítás)", task.getException());
                            }
                            Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });
        builder.setNegativeButton("Mégse", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void handleDeleteProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "Nincs bejelentkezve felhasználó.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Profil törlése")
                .setMessage("Biztosan törölni szeretné a profilját? Ez a művelet visszavonhatatlan.")
                .setPositiveButton("Igen", (dialog, which) -> {
                    promptForPasswordAndExecuteDeletion();
                })
                .setNegativeButton("Mégse", null)
                .show();
    }

    private void promptForPasswordAndExecuteDeletion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adja meg a jelszavát");
        builder.setMessage("A biztonság érdekében kérjük, adja meg a jelszavát a profil törléséhez.");

        final EditText passwordInput = new EditText(this);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(passwordInput);

        builder.setPositiveButton("Törlés", (dialog, which) -> {
            String password = passwordInput.getText().toString().trim();

            if (password.isEmpty()) {
                Toast.makeText(ProfileActivity.this, "A jelszó nem lehet üres.", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            currentUser.delete()
                                    .addOnCompleteListener(deleteTask -> {
                                        if (deleteTask.isSuccessful()) {
                                            Toast.makeText(ProfileActivity.this, "Profil sikeresen törölve!", Toast.LENGTH_LONG).show();
                                            mAuth.signOut();
                                            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            String errorMessage = "Hiba a profil törlésekor.";
                                            if (deleteTask.getException() != null) {
                                                errorMessage += " " + deleteTask.getException().getMessage();
                                                Log.e("ProfileActivity", "Profil törlése hiba", deleteTask.getException());
                                            }
                                            Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            String errorMessage = "Hibás jelszó vagy hiba a hitelesítéskor.";
                            if (task.getException() != null) {
                                errorMessage += " " + task.getException().getMessage();
                                Log.e("ProfileActivity", "Újra hitelesítés hiba (profil törlés)", task.getException());
                            }
                            Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });
        builder.setNegativeButton("Mégse", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadOrders() {
        if (currentUser == null) {
            Log.d("ProfileActivity", "Nincs bejelentkezett felhasználó a rendelések betöltéséhez.");
            return;
        }

        db.collection("users").document(currentUser.getUid()).collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Order> fetchedOrders = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            try {
                                Order order = document.toObject(Order.class);
                                if (order != null) {
                                    // A Firestore dokumentum ID-jét beállítjuk az Order objektumban
                                    order.setOrderId(document.getId());
                                    fetchedOrders.add(order);
                                }
                            } catch (Exception e) {
                                Log.e("ProfileActivity", "Hiba az Order objektum konvertálásakor: " + document.getId(), e);
                                // Debug: Logolja a nyers adatokat, ha probléma van a konvertálással
                                Log.e("ProfileActivity", "Nyers dokumentum adatok: " + document.getData());
                            }
                        }
                        orderAdapter.updateOrders(fetchedOrders);
                        Log.d("ProfileActivity", "Rendelések sikeresen betöltve: " + fetchedOrders.size());
                    } else {
                        Log.e("ProfileActivity", "Hiba a rendelések betöltésekor: ", task.getException());
                        Toast.makeText(ProfileActivity.this, "Hiba a rendelések betöltésekor.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}