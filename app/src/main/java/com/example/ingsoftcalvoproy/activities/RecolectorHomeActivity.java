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

    // Datos simulados de sesión
    private int userId = 2; // ejemplo de ID
    private String userName = "Recolector";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recolector_home);

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
            Toast.makeText(this, "Funcionalidad de reporte en construcción", Toast.LENGTH_SHORT).show();
            // Aquí iría la lógica para actualizar estados en la DB
        });

        // 🔹 Botón: Logout
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}

