package com.example.eskuvo;
import com.example.eskuvo.model.Order;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eskuvo.adapter.OrderAdapter;
import com.example.eskuvo.model.CartItem;
import com.example.eskuvo.model.Decoration;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firestore.admin.v1.Index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String LOG_TAG = ProfileActivity.class.getName();

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    private TextView tvName, tvEmail;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orders = new ArrayList<>();

    private void loadOrders() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orders.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        List<Map<String, Object>> itemsMap = (List<Map<String, Object>>) doc.get("items");
                        double totalPrice = doc.getDouble("totalPrice");
                        long timestamp = doc.getLong("timestamp");

                        List<CartItem> cartItems = new ArrayList<>();
                        for (Map<String, Object> itemMap : itemsMap) {
                            String name = (String) itemMap.get("name");
                            double price = ((Number) itemMap.get("price")).doubleValue();
                            int quantity = ((Number) itemMap.get("quantity")).intValue();
                            Decoration dec = new Decoration(name, price);
                            cartItems.add(new CartItem(dec, quantity));
                        }

                        orders.add(new Order(cartItems, totalPrice, timestamp));
                    }

                    orderAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Nem sikerült betölteni a rendeléseket: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_profile);

        // Toolbar és DrawerLayout init
        Toolbar toolbar = findViewById(R.id.tool_bar_profile);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout_profile);
        navigationView = findViewById(R.id.nav_view_profile);

        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Menü láthatóságának beállítása bejelentkezés alapján
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

        // TextView-okhoz referencia
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);

        // Felhasználó adatainak megjelenítése
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = "Név ismeretlen";
            }
            tvName.setText(name);
            tvEmail.setText(user.getEmail());
        } else {
            tvName.setText("Nincs bejelentkezve");
            tvEmail.setText("");
        }

        Log.i(LOG_TAG, "ProfileActivity onCreate");


        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(orders);
        ordersRecyclerView.setAdapter(orderAdapter);

// Rendelések betöltése
        loadOrders();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
