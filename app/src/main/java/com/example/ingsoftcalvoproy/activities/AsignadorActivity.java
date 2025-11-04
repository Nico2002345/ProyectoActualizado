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
    private Button btnAssignCollectors, btnManagePickups, btnAssignVehicles, btnLogout;

    // Simulación de datos de sesión
    private int userId = 1;
    private String userName = "Asignador";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignador);

        // 🔹 Inicializar vistas
        tvWelcome = findViewById(R.id.tvWelcome);
        tvRole = findViewById(R.id.tvRole);
        btnAssignCollectors = findViewById(R.id.btnAssignCollectors);
        btnManagePickups = findViewById(R.id.btnManagePickups);
        btnAssignVehicles = findViewById(R.id.btnAssignVehicles);
        btnLogout = findViewById(R.id.btnLogout);

        // 🔹 Configurar nombre y rol
        tvWelcome.setText("Bienvenido, " + userName);
        tvRole.setText("Rol: ASIGNADOR");

        // 🔹 Configurar botones y acciones con manejo seguro
        btnAssignCollectors.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, CollectorsListActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USER_NAME", userName);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al abrir lista de recolectores", Toast.LENGTH_LONG).show();
            }
        });

        btnManagePickups.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, PickupsListActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USER_NAME", userName);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al abrir lista de recogidas", Toast.LENGTH_LONG).show();
            }
        });

        btnAssignVehicles.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, TrucksListActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USER_NAME", userName);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al abrir lista de vehículos", Toast.LENGTH_LONG).show();
            }
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
