package com.example.ingsoftcalvoproy.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
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

public class RequestPickupActivity extends AppCompatActivity {

    private EditText etAddress, etWeight, etVolume, etScheduledTime;
    private Button btnRequestPickup;
    private ListView lvPickups;
    private TextView tvHistoryTitle;
    private ApiService api;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_pickup);

        // Obtener userId del Intent
        userId = getIntent().getIntExtra("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        api = ApiClient.getClient().create(ApiService.class);

        // Inicializar vistas
        etAddress = findViewById(R.id.etAddress);
        etWeight = findViewById(R.id.etWeight);
        etVolume = findViewById(R.id.etVolume);
        etScheduledTime = findViewById(R.id.etScheduledTime);
        btnRequestPickup = findViewById(R.id.btnRequestPickup);
        lvPickups = findViewById(R.id.lvPickups);
        tvHistoryTitle = findViewById(R.id.tvHistoryTitle);

        setTitle("Solicitar Recogida");

        btnRequestPickup.setOnClickListener(v -> requestPickup());

        // Cargar historial de pickups del usuario
        loadUserPickups();
    }

    private void requestPickup() {
        String address = etAddress.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String volumeStr = etVolume.getText().toString().trim();
        String scheduledTime = etScheduledTime.getText().toString().trim();

        // Validaciones
        if (address.isEmpty() || weightStr.isEmpty() || volumeStr.isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight, volume;
        try {
            weight = Double.parseDouble(weightStr);
            volume = Double.parseDouble(volumeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingrese valores numéricos válidos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear body para el API
        Map<String, Object> body = new HashMap<>();
        body.put("user", userId);
        body.put("address", address);
        body.put("weight_kg", weight);
        body.put("volume_m3", volume);

        // Scheduled time es opcional
        if (!scheduledTime.isEmpty()) {
            body.put("scheduled_at", scheduledTime);
        }

        // Enviar solicitud
        api.createPickup(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Utils.toast(RequestPickupActivity.this, "✅ Solicitud de recogida creada correctamente");

                    // Limpiar campos
                    etAddress.setText("");
                    etWeight.setText("");
                    etVolume.setText("");
                    etScheduledTime.setText("");

                    // Recargar el historial
                    loadUserPickups();
                } else {
                    Utils.toast(RequestPickupActivity.this, "Error al crear la solicitud");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Utils.toast(RequestPickupActivity.this, "Error de red: " + t.getMessage());
            }
        });
    }

    private void loadUserPickups() {
        // Nota: El endpoint getPickupsByUser está comentado pero debería devolver Map, no ShipmentDTO
        // Por ahora usamos el endpoint correcto
        api.getAllPickupsPending().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    tvHistoryTitle.setVisibility(View.GONE);
                    lvPickups.setVisibility(View.GONE);
                    return;
                }

                List<String> pickupsList = new ArrayList<>();
                for (Map<String, Object> pickup : response.body()) {
                    // Filtrar solo los del usuario actual
                    Object userIdObj = pickup.get("user");
                    int pickupUserId = -1;
                    if (userIdObj instanceof Number) {
                        pickupUserId = ((Number) userIdObj).intValue();
                    }

                    if (pickupUserId != userId) {
                        continue;
                    }

                    String status = (String) pickup.get("status");
                    String address = (String) pickup.get("address");
                    double weight = pickup.get("weight_kg") != null ? ((Number) pickup.get("weight_kg")).doubleValue() : 0;
                    double volume = pickup.get("volume_m3") != null ? ((Number) pickup.get("volume_m3")).doubleValue() : 0;

                    String info = String.format(
                            "📦 Estado: %s\n📍 Dirección: %s\n⚖ Peso: %.2f kg | Vol: %.3f m³",
                            status != null ? status : "PENDIENTE",
                            address != null ? address : "Sin dirección",
                            weight,
                            volume
                    );
                    pickupsList.add(info);
                }

                if (pickupsList.isEmpty()) {
                    tvHistoryTitle.setVisibility(View.GONE);
                    lvPickups.setVisibility(View.GONE);
                } else {
                    tvHistoryTitle.setVisibility(View.VISIBLE);
                    lvPickups.setVisibility(View.VISIBLE);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            RequestPickupActivity.this,
                            android.R.layout.simple_list_item_1,
                            pickupsList
                    );
                    lvPickups.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Utils.toast(RequestPickupActivity.this, "Error cargando historial: " + t.getMessage());
            }
        });
    }
}
