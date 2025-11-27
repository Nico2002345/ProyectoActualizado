package com.example.ingsoftcalvoproy.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.dto.ShipmentDTO;
import com.example.ingsoftcalvoproy.network.ApiClient;
import com.example.ingsoftcalvoproy.network.ApiService;
import com.example.ingsoftcalvoproy.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Lista de solicitudes de recogida (pickups) usando API.
 * Muestra las solicitudes del usuario o del recolector asignado.
 */
public class PickupsListActivity extends AppCompatActivity {

    private ListView lvPickups;
    private ArrayList<String> data = new ArrayList<>();
    private ArrayList<Integer> ids = new ArrayList<>();
    private ApiService apiService;

    private boolean isCollectorView = false;
    private boolean showCompleted = false; // Nuevo parámetro
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickups_list);

        // Obtener userId del Intent
        currentUserId = getIntent().getIntExtra("USER_ID", -1);

        if (currentUserId == -1) {
            Utils.toast(this, "Error: Usuario no identificado");
            finish();
            return;
        }

        // Determinar si es vista de recolector (se puede pasar como extra o detectar por rol)
        String userRole = getIntent().getStringExtra("USER_ROLE");
        isCollectorView = "RECOLECTOR".equalsIgnoreCase(userRole);

        // Determinar si debe mostrar completadas o pendientes
        showCompleted = getIntent().getBooleanExtra("SHOW_COMPLETED", false);

        lvPickups = findViewById(R.id.lvPickups);
        apiService = ApiClient.getClient().create(ApiService.class);

        // Actualizar título según el tipo de vista
        if (showCompleted) {
            setTitle("Recogidas Completadas");
        } else {
            setTitle("Recogidas Pendientes");
        }

        loadPickups();

        // 🔹 Permite marcar recogida como completada (solo recolector y solo en vista de pendientes)
        lvPickups.setOnItemClickListener((parent, view, position, id) -> {
            if (isCollectorView && !showCompleted) {
                int pickupId = ids.get(position);
                markPickupCompleted(pickupId);
            }
        });
    }

    private void loadPickups() {
        data.clear();
        ids.clear();

        // Decidir qué endpoint llamar según showCompleted
        Call<List<Map<String, Object>>> call;
        if (showCompleted) {
            call = apiService.getAllPickupsCompleted();
        } else {
            call = apiService.getAllPickupsPending();
        }

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Map<String, Object> pickup : response.body()) {
                        // Obtener datos del pickup
                        int pickupId = pickup.get("id") != null ? ((Number) pickup.get("id")).intValue() : -1;
                        int userId = pickup.get("user") != null ? ((Number) pickup.get("user")).intValue() : -1;
                        Object collectorObj = pickup.get("collector");
                        Integer collectorId = null;
                        if (collectorObj instanceof Number) {
                            collectorId = ((Number) collectorObj).intValue();
                        }

                        String address = (String) pickup.get("address");
                        String status = (String) pickup.get("status");
                        double weight = pickup.get("weight_kg") != null ? ((Number) pickup.get("weight_kg")).doubleValue() : 0;
                        double volume = pickup.get("volume_m3") != null ? ((Number) pickup.get("volume_m3")).doubleValue() : 0;

                        // Si es vista de recolector, mostrar solo los asignados a él
                        // (Nota: esto requiere vincular collector_id con el user_id del recolector)
                        // Por ahora, mostramos todos para el recolector

                        // Si es vista de usuario, mostrar solo los de ese usuario
                        if (!isCollectorView && userId != currentUserId) {
                            continue;
                        }

                        String displayText = String.format(
                                "📦 ID: %d\n📍 %s\n⚖ Peso: %.2f kg | Vol: %.3f m³\n📊 Estado: %s",
                                pickupId,
                                address != null ? address : "Sin dirección",
                                weight,
                                volume,
                                status != null ? status : "PENDIENTE"
                        );

                        data.add(displayText);
                        ids.add(pickupId);
                    }

                    if (data.isEmpty()) {
                        Utils.toast(PickupsListActivity.this, "No hay recogidas pendientes.");
                    }

                    lvPickups.setAdapter(new ArrayAdapter<>(PickupsListActivity.this,
                            android.R.layout.simple_list_item_1, data));
                } else {
                    Utils.toast(PickupsListActivity.this, "No hay pickups disponibles.");
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Utils.toast(PickupsListActivity.this, "Error al cargar pickups: " + t.getMessage());
            }
        });
    }

    private void markPickupCompleted(int pickupId) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "COLECTADA");

        apiService.updatePickupStatus(pickupId, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Utils.toast(PickupsListActivity.this, "✅ Recogida marcada como completada.");
                    loadPickups();
                } else {
                    Utils.toast(PickupsListActivity.this, "Error al actualizar el estado.");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Utils.toast(PickupsListActivity.this, "Error de red: " + t.getMessage());
            }
        });
    }
}