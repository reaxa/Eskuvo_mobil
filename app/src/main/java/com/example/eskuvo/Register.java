package com.example.eskuvo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Register extends AppCompatActivity {

    private static final String LOG_TAG = Register.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();

    EditText userNameEditText;
    EditText userEmailEditText;
    EditText passwordEditText;
    EditText password2EditText;

    private SharedPreferences preferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        userNameEditText = findViewById(R.id.editTextUserName);
        userEmailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        password2EditText = findViewById(R.id.editTextPassword2);


        //Bundle bundle = getIntent().getExtras();
        //bundle.get("SECRET_KEY");
        int secret_key= getIntent().getIntExtra("SECRET_KEY", 0);

        if (secret_key != 88){
            finish();
        }

          Log.i(LOG_TAG, "onCreate");


        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String userName= preferences.getString("userName", "");
        String password= preferences.getString("password", "");

        userNameEditText.setText(userName);
        passwordEditText.setText(password);
        password2EditText.setText(password);


    }

    public void register(View view) {
        String userName= userNameEditText.getText().toString();
        String email= userEmailEditText.getText().toString();
        String password= passwordEditText.getText().toString();
        String password2 = password2EditText.getText().toString();

        Log.i(LOG_TAG, "Bejelentkezett: " + userNameEditText);

        if(!password.equals(password2)){
            Log.e(LOG_TAG, "Nem egyenlő a két jelszó!");
        }


    }

    public void login(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("SECRET_KEY", 66);
        startActivity(intent);
    }


 



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
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
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");
    }


}