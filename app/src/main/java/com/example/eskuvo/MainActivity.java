package com.example.eskuvo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
private static final String LOG_TAG = MainActivity.class.getName();
private static final String PREF_KEY = MainActivity.class.getPackage().toString();
private static final int SECRET_KEY = 88;

    EditText userEmailET;
    EditText userPasswordET;



    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userEmailET = findViewById(R.id.editTextEmail);
        userPasswordET = findViewById(R.id.editTextPassword);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);


        Log.i(LOG_TAG, "onCreate");


    }
    public void login (View view) {
        String userName = userEmailET.getText().toString();
        String password = userPasswordET.getText().toString();
        Log.i(LOG_TAG, "Bejelentkezett: " + userEmailET);
}

    public void register(View view) {
        Intent intent = new Intent(this, Register.class);
        intent.putExtra("SECRET_KEY", 88);
        startActivity(intent);
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