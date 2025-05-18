package com.example.eskuvo;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eskuvo.adapter.DecorationAdapter;

import com.example.eskuvo.MainActivity;
import com.example.eskuvo.R;
import com.example.eskuvo.decorations;
import com.example.eskuvo.model.Decoration;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import android.util.Log;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.util.List;


public class decorations extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private List<Decoration> decorationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decorations);

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

        // JSON beolvasása
        try {
            InputStreamReader reader = new InputStreamReader(getAssets().open("decorations.json"));
            java.lang.reflect.Type type = new TypeToken<List<Decoration>>() {}.getType();
            decorationList = new Gson().fromJson(reader, type);
            RecyclerView recyclerView = findViewById(R.id.decorationsRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            DecorationAdapter adapter = new DecorationAdapter(this, decorationList, user);
            recyclerView.setAdapter(adapter);
            if (decorationList != null && !decorationList.isEmpty()) {
                Log.d("DECORATIONS_DEBUG", "Első dekoráció neve: " + decorationList.get(0).getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
