package com.example.ingsoftcalvoproy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;

public class AsignadorActivity extends AppCompatActivity {

    private TextView tvWelcome, tvRole;
    private Button btnAssignPickups, btnLogout;

    private int userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignador);

        // Obtener datos del Intent
        userId = getIntent().getIntExtra("USER_ID", -1);
        userName = getIntent().getStringExtra("USER_NAME");

        if (userId == -1 || userName == null) {
            userId = 1;
            userName = "Asignador";
        }

        // 🔹 Inicializar vistas
        tvWelcome = findViewById(R.id.tvWelcome);
        tvRole = findViewById(R.id.tvRole);
        btnAssignPickups = findViewById(R.id.btnAssignPickups);
        btnLogout = findViewById(R.id.btnLogout);

        // 🔹 Configurar nombre y rol
        tvWelcome.setText("Bienvenido, " + userName);
        tvRole.setText("Rol: ASIGNADOR");

        // 🔹 Botón: Asignar Recolector y Camión
        btnAssignPickups.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, AssignPickupsActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USER_NAME", userName);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al abrir asignación", Toast.LENGTH_LONG).show();
            }
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}