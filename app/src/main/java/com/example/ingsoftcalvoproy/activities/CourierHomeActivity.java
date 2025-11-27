package com.example.ingsoftcalvoproy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.dto.ShipmentDTO;
import com.example.ingsoftcalvoproy.network.ApiClient;
import com.example.ingsoftcalvoproy.network.ApiService;
import com.example.ingsoftcalvoproy.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla principal del repartidor usando API.
 */
public class CourierHomeActivity extends AppCompatActivity {

    private TextView tvTruckInfo, tvStats;
    private Button btnVerGuias, btnRegistrarEntrega, btnActualizarEstado;
    private Button btnLogout;
    private android.widget.Spinner spShipments;

    private ApiService api;
    private int userId;
    private String userName;
    private String userRole;
    private final List<ShipmentDTO> availableShipments = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_home);

        tvTruckInfo = findViewById(R.id.tvTruckInfo);
        tvStats = findViewById(R.id.tvStats);
        btnVerGuias = findViewById(R.id.btnVerGuias);
        btnRegistrarEntrega = findViewById(R.id.btnRegistrarEntrega);
        btnActualizarEstado = findViewById(R.id.btnActualizarEstado);
        spShipments = findViewById(R.id.spShipments);
        btnLogout = findViewById(R.id.btnLogout);

        api = ApiClient.getClient().create(ApiService.class);

        // Datos del conductor
        userId = getIntent().getIntExtra("USER_ID", -1);
        userName = getIntent().getStringExtra("USER_NAME");
        userRole = getIntent().getStringExtra("USER_ROLE");
        setTitle("Panel del " + (userRole != null ? userRole : "Repartidor"));

        loadTruckData();
        loadStats();
        loadAvailableShipments();

        btnVerGuias.setOnClickListener(v ->
                startActivity(new Intent(this, GuidesListActivity.class))
        );

        btnRegistrarEntrega.setOnClickListener(v ->
                startActivity(new Intent(this, TrackingActivity.class))
        );

        btnActualizarEstado.setOnClickListener(v -> markDelivered());

        // 🔹 LOGOUT — colocado en el lugar CORRECTO
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(CourierHomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /** Carga la info del camión asignado usando API */
    private void loadTruckData() {
        api.getTrucksByRepartidor(userId).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    tvTruckInfo.setText("🚫 No hay camiones asignados.");
                    return;
                }

                if (response.body().size() == 1) {
                    Map<String, Object> truck = response.body().get(0);
                    String plate = (String) truck.get("plate");
                    double capacity = 0;
                    Object capObj = truck.get("capacity_kg");
                    if (capObj instanceof Double) capacity = (Double) capObj;
                    else if (capObj instanceof Integer) capacity = ((Integer) capObj).doubleValue();

                    tvTruckInfo.setText("🚚 Camión asignado: " + plate +
                            "\nCapacidad: " + capacity + " kg");
                } else {
                    StringBuilder sb = new StringBuilder("🚚 Camiones asignados:\n");
                    for (Map<String, Object> truck : response.body()) {
                        String plate = (String) truck.get("plate");
                        sb.append("• ").append(plate).append("\n");
                    }
                    tvTruckInfo.setText(sb.toString());
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                tvTruckInfo.setText("Error cargando camión");
            }
        });
    }

    /** Carga los envíos disponibles */
    private void loadAvailableShipments() {
        api.getShipmentsByAssignedUser(userId).enqueue(new Callback<List<ShipmentDTO>>() {
            @Override
            public void onResponse(Call<List<ShipmentDTO>> call, Response<List<ShipmentDTO>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                availableShipments.clear();
                List<String> shipmentNames = new java.util.ArrayList<>();

                for (ShipmentDTO s : response.body()) {
                    String status = s.getStatus();
                    if ("ASIGNADO".equals(status) || "EN_TRANSITO".equals(status) || "EN_TRÁNSITO".equals(status)) {
                        availableShipments.add(s);
                        shipmentNames.add(s.getShipmentCode() + " - " + status);
                    }
                }

                if (availableShipments.isEmpty()) {
                    shipmentNames.add("No hay envíos asignados");
                }

                android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                        CourierHomeActivity.this,
                        android.R.layout.simple_spinner_item,
                        shipmentNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spShipments.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<ShipmentDTO>> call, Throwable t) {
                Utils.toast(CourierHomeActivity.this, "Error cargando envíos disponibles");
            }
        });
    }

    /** Estadísticas */
    private void loadStats() {
        api.getShipmentsByAssignedUser(userId).enqueue(new Callback<List<ShipmentDTO>>() {
            @Override
            public void onResponse(Call<List<ShipmentDTO>> call, Response<List<ShipmentDTO>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvStats.setText("📦 Mis Envíos:\nPendientes: 0\nEn tránsito: 0\nEntregados: 0");
                    return;
                }

                int pendientes = 0, transito = 0, entregados = 0;

                for (ShipmentDTO shipment : response.body()) {
                    String status = shipment.getStatus();
                    if ("CREADO".equals(status) || "PENDIENTE".equals(status) || "ASIGNADO".equals(status)) {
                        pendientes++;
                    } else if ("EN_TRANSITO".equals(status) || "EN_TRÁNSITO".equals(status)) {
                        transito++;
                    } else if ("ENTREGADO".equals(status)) {
                        entregados++;
                    }
                }

                tvStats.setText("📦 Mis Envíos:\n" +
                        "Pendientes: " + pendientes + "\n" +
                        "En tránsito: " + transito + "\n" +
                        "Entregados: " + entregados);
            }

            @Override
            public void onFailure(Call<List<ShipmentDTO>> call, Throwable t) {
                Utils.toast(CourierHomeActivity.this, "Error cargando estadísticas");
                tvStats.setText("📦 Mis Envíos:\nError al cargar");
            }
        });
    }

    /** Actualizar estado */
    private void markDelivered() {
        int selectedPosition = spShipments.getSelectedItemPosition();

        if (availableShipments.isEmpty()) {
            Utils.toast(this, "No hay envíos disponibles para actualizar");
            return;
        }

        if (selectedPosition < 0 || selectedPosition >= availableShipments.size()) {
            Utils.toast(this, "Por favor selecciona un envío");
            return;
        }

        ShipmentDTO selectedShipment = availableShipments.get(selectedPosition);

        String currentStatus = selectedShipment.getStatus();
        String newStatus;
        String mensaje;

        if ("ASIGNADO".equals(currentStatus)) {
            newStatus = "EN_TRANSITO";
            mensaje = "📦 Envío marcado EN TRÁNSITO";
        } else {
            newStatus = "ENTREGADO";
            mensaje = "✅ Entrega confirmada";
        }

        Map<String, Object> body = new HashMap<>();
        body.put("status", newStatus);

        api.updateShipmentStatus(selectedShipment.getId(), body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Utils.toastLong(CourierHomeActivity.this, mensaje + " para " + selectedShipment.getShipmentCode());
                    loadStats();
                    loadAvailableShipments();
                } else {
                    Utils.toast(CourierHomeActivity.this, "Error actualizando estado");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Utils.toast(CourierHomeActivity.this, "Error actualizando estado: " + t.getMessage());
            }
        });
    }
}
