package com.example.ingsoftcalvoproy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;

public class RecolectorHomeActivity extends AppCompatActivity {

    private TextView tvWelcome, tvRole;
    private Button btnViewPickups, btnTrackRoute, btnReportCompletion, btnLogout;

    private int userId;
    private String userName;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recolector_home);

        // Obtener datos del Intent
        userId = getIntent().getIntExtra("USER_ID", -1);
        userName = getIntent().getStringExtra("USER_NAME");
        userRole = getIntent().getStringExtra("USER_ROLE");

        if (userId == -1) {
            userName = "Recolector";
            userRole = "RECOLECTOR";
        }

        // 🔹 Inicializar vistas
        tvWelcome = findViewById(R.id.tvWelcome);
        tvRole = findViewById(R.id.tvRole);

        btnViewPickups = findViewById(R.id.btnViewPickups);
        btnTrackRoute = findViewById(R.id.btnTrackRoute);
        btnReportCompletion = findViewById(R.id.btnReportCompletion);
        btnLogout = findViewById(R.id.btnLogout);

        // 🔹 Configurar nombre y rol
        tvWelcome.setText("Bienvenido, " + userName);
        tvRole.setText("Rol: RECOLECTOR");

        // 🔹 Botón: Ver Recogidas
        btnViewPickups.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, PickupsListActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USER_NAME", userName);
                intent.putExtra("USER_ROLE", userRole);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al abrir recogidas", Toast.LENGTH_LONG).show();
            }
        });

        // 🔹 Botón: Ver Ruta
        btnTrackRoute.setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidad de ruta en construcción", Toast.LENGTH_SHORT).show();
            // Aquí podrías abrir un MapActivity o mostrar ruta optimizada
        });

        // 🔹 Botón: Reportar Completadas
        btnReportCompletion.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, PickupsListActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USER_NAME", userName);
                intent.putExtra("USER_ROLE", userRole);
                intent.putExtra("SHOW_COMPLETED", true); // Mostrar completadas
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al abrir completadas", Toast.LENGTH_LONG).show();
            }
        });

        // 🔹 Botón: Logout
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}