package com.example.ingsoftcalvoproy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.utils.Utils;

/**
 * Pantalla principal para USUARIOS (clientes).
 * Muestra el nombre y rol, y permite navegar a los módulos principales.
 */
public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome, tvRole;
    private Button btnShipments, btnAnalytics, btnTracking, btnPickups, btnLogout;

    private int userId;
    private String userName;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // === Referencias ===
        tvWelcome = findViewById(R.id.tvWelcome);
        tvRole = findViewById(R.id.tvRole);
        btnShipments = findViewById(R.id.btnShipments);
        btnAnalytics = findViewById(R.id.btnAnalytics);
        btnTracking = findViewById(R.id.btnTracking);
        btnPickups = findViewById(R.id.btnPickups);
        btnLogout = findViewById(R.id.btnLogout);

        // === Obtener datos del usuario desde el intent ===
        userId = getIntent().getIntExtra("USER_ID", -1);
        userName = getIntent().getStringExtra("USER_NAME");
        userRole = getIntent().getStringExtra("USER_ROLE");

        // === Mostrar datos básicos ===
        tvWelcome.setText("Bienvenido, " + (userName != null ? userName : "Usuario"));
        tvRole.setText("Rol: " + (userRole != null ? userRole : "USUARIO"));

        // === Navegación ===
        btnShipments.setOnClickListener(v ->
                startActivity(new Intent(this, ShipmentListActivity.class))
        );

        btnAnalytics.setOnClickListener(v ->
                startActivity(new Intent(this, AnalyticsActivity.class))
        );

        btnTracking.setOnClickListener(v ->
                startActivity(new Intent(this, TrackingActivity.class))
        );

        btnPickups.setOnClickListener(v ->
                startActivity(new Intent(this, PickupsListActivity.class))
        );

        btnLogout.setOnClickListener(v -> logout());
    }

    /**
     * Cierra sesión y regresa al login.
     */
    private void logout() {
        Utils.toast(this, "Sesión finalizada.");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
