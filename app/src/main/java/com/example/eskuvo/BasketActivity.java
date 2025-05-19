package com.example.eskuvo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eskuvo.adapter.BasketAdapter;
import com.example.eskuvo.model.CartItem;
import com.example.eskuvo.service.CartManager;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;




public class BasketActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BasketAdapter basketAdapter;
    private TextView totalPriceTextView;
    private Button payButton;
    private DrawerLayout drawerLayout;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private void saveOrderToFirestore() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Nem vagy bejelentkezve, nem lehet menteni a rendelést.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<CartItem> currentItems = CartManager.getInstance().getCartItems();
        double total = CartManager.getInstance().getTotalPrice();

        // Átalakítás Map-ekre, mert Firestore nem tárolja a saját osztályokat közvetlenül
        List<Map<String, Object>> itemsMap = new ArrayList<>();
        for (CartItem ci : currentItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", ci.getDecoration().getName());
            itemMap.put("price", ci.getDecoration().getPrice());
            itemMap.put("quantity", ci.getQuantity());
            itemsMap.add(itemMap);
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("items", itemsMap);
        orderData.put("totalPrice", total);
        orderData.put("timestamp", System.currentTimeMillis());

        // Mentés a Firestore 'orders' kollekcióba, a felhasználóhoz kötve
        db.collection("users").document(user.getUid())
                .collection("orders")
                .add(orderData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Rendelés mentve.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba történt a mentés során: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);

        recyclerView = findViewById(R.id.recyclerViewBasket);
        totalPriceTextView = findViewById(R.id.textViewTotalPrice);
        payButton = findViewById(R.id.buttonPay);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        basketAdapter = new BasketAdapter(cartItems, this);
        recyclerView.setAdapter(basketAdapter);

        updateTotalPrice();
        basketAdapter.setQuantityChangeListener(() -> {
            updateTotalPrice();
        });
        payButton.setOnClickListener(v -> {
            saveOrderToFirestore();
            long tenMinutesFromNow = System.currentTimeMillis() + 10 * 60 * 1000;
            scheduleOrderNotification(tenMinutesFromNow, "A rendelésed feldolgozás alatt áll.");

            CartManager.getInstance().clearCart();
            Toast.makeText(this, "Fizetés megtörtént!", Toast.LENGTH_SHORT).show();
            finish(); // vissza előző képernyőre
        });

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
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
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
    }

    private void updateTotalPrice() {
        double total = CartManager.getInstance().getTotalPrice();
        totalPriceTextView.setText(String.format("Végösszeg: %.2f Ft", total));
    }


    public interface QuantityChangeListener {
        void onQuantityChanged();
    }
}