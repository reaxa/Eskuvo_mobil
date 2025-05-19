package com.example.eskuvo; // Győződj meg róla, hogy a csomag neve helyes

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

// Feltételezzük, hogy léteznek ezek az osztályok, amiket használsz
// import com.example.eskuvo.model.CartItem; // Ha van ilyen osztályod
// import com.example.eskuvo.model.Order;    // Ha van ilyen osztályod
// import com.example.eskuvo.adapter.OrderAdapter; // Ha van ilyen adaptered

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private TextView tvName; // Hozzáadtam a neved megjelenítéséhez
    private TextView tvEmail;
    private Button changeEmailButton;
    private Button deleteProfileButton;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private RecyclerView ordersRecyclerView;
    private FirebaseFirestore db;

    // Ha használsz kosár elemeket itt, akkor itt tárolhatod őket
    // private List<CartItem> cartItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Toolbar és navigációs fiók beállítása
        toolbar = findViewById(R.id.tool_bar_profile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Eltünteti a címet, ha beállítottad app:title-t XML-ben
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

        // UI elemek inicializálása
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        changeEmailButton = findViewById(R.id.change_email_button);
        deleteProfileButton = findViewById(R.id.delete_profile_button);
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);

        // RecyclerView beállítása
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // ordersRecyclerView.setAdapter(new OrderAdapter(new ArrayList<>())); // Inicializáld az adaptert egy üres listával

        // Navigációs menü elemek láthatóságának beállítása
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

        // Navigációs menü elemek kattintáskezelése
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
                // Már a profil oldalon vagyunk, nem kell újraindítani
                // startActivity(new Intent(this, ProfileActivity.class));
            } else if (id == R.id.nav_basket) {
                startActivity(new Intent(this, BasketActivity.class));
            } else if (id == R.id.nav_logout) {
                mAuth.signOut();
                Toast.makeText(ProfileActivity.this, "Sikeresen kijelentkezett!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class)); // Vagy a login képernyőre
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Felhasználói adatok betöltése és gombok láthatósága
        if (currentUser != null && !currentUser.isAnonymous()) {
            tvEmail.setText(getString(R.string.email_format, currentUser.getEmail()));

            // Ha tárolod a felhasználó nevét Firestore-ban (pl. "users" kollekcióban)
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userName = documentSnapshot.getString("name"); // Feltételezve, hogy van "name" mező
                            if (userName != null) {
                                tvName.setText(getString(R.string.neved_format, userName)); // Pl. "Neved: %s"
                            } else {
                                tvName.setText(getString(R.string.nevednem)); // Alapértelmezett, ha nincs név
                            }
                        } else {
                            tvName.setText(getString(R.string.neved));
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

            // Rendelések betöltése
            loadOrders();

            // Ide kerülhetne a "cartItems.add(new CartItem(dec, quantity));" logika,
            // ha valamiért itt akarod kezelni a kosárba adást.
            // Például:
            // CartItem dec = new CartItem("Dekoráció 1", 1000); // Példa Dekoráció
            // int quantity = 2; // Példa mennyiség
            // cartItems.add(new CartItem(dec, quantity)); // Ez a sor került volna be
            // Log.d("ProfileActivity", "Kosár elem hozzáadva (példa): " + cartItems.size());

        } else {
            // Nincs bejelentkezve vagy anonim felhasználó
            tvName.setText(getString(R.string.neved)); // Alapértelmezett
            tvEmail.setText(getString(R.string.email_format, "Nincs bejelentkezve"));
            changeEmailButton.setVisibility(View.GONE);
            deleteProfileButton.setVisibility(View.GONE);
            Toast.makeText(this, "Jelentkezzen be a profilfunkciók eléréséhez.", Toast.LENGTH_LONG).show();
            // Optional: Redirect to login activity
            // startActivity(new Intent(this, MainActivity.class));
            // finish();
        }
    }

    // A kosár elem hozzáadása valószínűleg nem idevaló,
    // de ha mégis, akkor valahonnan kellene kapnia a 'dec' és 'quantity' értékeket.
    // Ha a 210-es sorban lévő kód a megadott, akkor az egy metóduson belül kellene lennie,
    // pl. egy gombnyomásra vagy valamilyen eseményre aktiválódva.
    // Példa (ez valószínűleg ROSSZ helyen van a ProfileActivity-ben):
    /*
    private void addDecorationToCart(Decoration dec, int quantity) {
        cartItems.add(new CartItem(dec, quantity));
        Toast.makeText(this, dec.getName() + " hozzáadva a kosárhoz!", Toast.LENGTH_SHORT).show();
        // Frissíteni kellene a kosár UI-t is, ha van ilyen.
    }
    */


    // Email cím módosítása
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

    // Profil törlése
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

    // Rendelések betöltése Firestore-ból
    private void loadOrders() {
        if (currentUser == null) {
            Log.d("ProfileActivity", "Nincs bejelentkezett felhasználó a rendelések betöltéséhez.");
            return;
        }

        db.collection("users").document(currentUser.getUid()).collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Feltételezve, hogy van 'timestamp' meződ
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Object> orders = new ArrayList<>(); // Cseréld ki 'Object'-et a tényleges 'Order' osztályodra
                        for (DocumentSnapshot document : task.getResult()) {
                            // Cseréld ki az 'Order.class'-t a tényleges rendelési osztályodra
                            // Order order = document.toObject(Order.class);
                            // if (order != null) {
                            //     orders.add(order);
                            // }
                        }
                        // Itt frissítsd a RecyclerView adapterét a betöltött rendelésekkel
                        // Pl.: ordersRecyclerView.setAdapter(new OrderAdapter(orders));
                        Log.d("ProfileActivity", "Rendelések sikeresen betöltve: " + orders.size());
                    } else {
                        Log.e("ProfileActivity", "Hiba a rendelések betöltésekor: ", task.getException());
                        Toast.makeText(ProfileActivity.this, "Hiba a rendelések betöltésekor.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // A navigációs fiók bezárásának kezelése visszagombnyomásra
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}