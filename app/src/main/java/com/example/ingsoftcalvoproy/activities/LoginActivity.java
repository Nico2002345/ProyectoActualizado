package com.example.ingsoftcalvoproy.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;

/**
 * Permite iniciar sesión según el rol.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private Db db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new Db(this);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String email = etEmail.getText().toString();
        String pass = etPassword.getText().toString();

        Cursor c = db.raw("SELECT id, name, role FROM users WHERE email=? AND password=?",
                new String[]{email, pass});
        if (c.moveToFirst()) {
            String role = c.getString(2);
            Toast.makeText(this, "Bienvenido " + role, Toast.LENGTH_SHORT).show();

            if ("REPARTIDOR".equalsIgnoreCase(role))
                startActivity(new Intent(this, CourierHomeActivity.class));
            else
                startActivity(new Intent(this, HomeActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Credenciales inválidas", Toast.LENGTH_SHORT).show();
        }
        c.close();
    }
}
