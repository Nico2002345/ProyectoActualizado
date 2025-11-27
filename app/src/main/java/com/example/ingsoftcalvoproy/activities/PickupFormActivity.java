package com.example.ingsoftcalvoproy.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.network.ApiService;
import com.example.ingsoftcalvoproy.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Formulario para crear una nueva solicitud de recogida (pickup) vía API.
 */
public class PickupFormActivity extends AppCompatActivity {

    private EditText etAddress, etDate, etWeight, etVolume, etEmail;
    private Button btnSavePickup;

    // 🔹 Simulación temporal de usuario (cuando se implemente login, se pasará desde sesión)
    private int currentUserId = 1;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_form);

        etAddress = findViewById(R.id.etAddress);
        etDate = findViewById(R.id.etDate);
        etWeight = findViewById(R.id.etWeight);
        etVolume = findViewById(R.id.etVolume);
        etEmail = findViewById(R.id.etEmail);
        btnSavePickup = findViewById(R.id.btnSavePickup);

        // 🔹 Inicializar Retrofit API
        apiService = Utils.getApiService();

        btnSavePickup.setOnClickListener(v -> savePickup());
    }

    private void savePickup() {
        String address = etAddress.getText().toString();
        String date = etDate.getText().toString();
        String email = etEmail.getText().toString();

        if (Utils.isEmpty(address) || Utils.isEmpty(date)) {
            Utils.toast(this, "Por favor completa la dirección y la fecha.");
            return;
        }

        double weight = Utils.parseDoubleSafe(etWeight.getText().toString());
        double volume = Utils.parseDoubleSafe(etVolume.getText().toString());

        // Código de recogida
        String pickupCode = "PKP-" + Utils.generateShipmentCode().substring(4);

        // Crear cuerpo de la solicitud
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", currentUserId);
        body.put("address", address);
        body.put("scheduled_at", date);
        body.put("weight_kg", weight);
        body.put("volume_m3", volume);
        body.put("status", "PENDIENTE");
        body.put("pickup_code", pickupCode);

        // 🔹 Llamada a la API
        apiService.createPickup(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Utils.toastLong(PickupFormActivity.this, "✅ Recogida registrada con código: " + pickupCode);

                    // Enviar comprobante por email
                    if (!Utils.isEmpty(email) && Utils.isValidEmail(email)) {
                        String bodyEmail = "📦 Comprobante de Solicitud de Recogida\n" +
                                "Código: " + pickupCode + "\n" +
                                "Dirección: " + address + "\n" +
                                "Fecha programada: " + date + "\n" +
                                "Peso estimado: " + weight + " kg\n" +
                                "Volumen estimado: " + volume + " m³\n" +
                                "Estado actual: PENDIENTE\n" +
                                "Fecha de solicitud: " + Utils.now();

                        Utils.sendEmail(PickupFormActivity.this, email, "Comprobante de Recogida " + pickupCode, bodyEmail);
                    }

                    finish();
                } else {
                    Utils.toast(PickupFormActivity.this, "❌ Error al registrar la recogida.");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Utils.toast(PickupFormActivity.this, "❌ Error de conexión: " + t.getMessage());
            }
        });
    }
}
