package com.example.ingsoftcalvoproy.activities;

import static com.example.ingsoftcalvoproy.utils.Utils.generateShipmentCode;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class ShipmentFormActivity extends AppCompatActivity {

    private EditText etObject, etAddress, etWeight, etDistance, etVolume;
    private Button btnSaveShipment;
    private ApiService apiService;
    private int userId = 1; // Reemplazar con ID real del usuario logueado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment_form);

        apiService = Utils.getApiService();

        etObject = findViewById(R.id.etObject);
        etAddress = findViewById(R.id.etAddress);
        etWeight = findViewById(R.id.etWeight);
        etDistance = findViewById(R.id.etDistance);
        etVolume = findViewById(R.id.etVolume);

        btnSaveShipment = findViewById(R.id.btnSaveShipment);
        btnSaveShipment.setOnClickListener(v -> saveShipment());

        // Actualizar volumen automáticamente al cambiar peso o distancia
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateVolume(); }
            @Override public void afterTextChanged(Editable s) {}
        };

        etWeight.addTextChangedListener(watcher);
        etDistance.addTextChangedListener(watcher);
    }

    private void updateVolume() {
        double weight = Utils.parseDoubleSafe(etWeight.getText().toString());
        double distance = Utils.parseDoubleSafe(etDistance.getText().toString());
        double volume = weight * distance;

        // Solo actualizar si el usuario no ha escrito manualmente algo distinto
        String currentVolume = etVolume.getText().toString();
        if (Utils.isEmpty(currentVolume) || Double.parseDouble(currentVolume) != volume) {
            etVolume.setText(String.valueOf(volume));
        }
    }

    private void saveShipment() {
        if (Utils.isEmpty(etObject.getText().toString())
                || Utils.isEmpty(etAddress.getText().toString())
                || Utils.isEmpty(etWeight.getText().toString())
                || Utils.isEmpty(etDistance.getText().toString())
                || Utils.isEmpty(etVolume.getText().toString())) {
            Utils.toast(this, "Completa todos los campos obligatorios.");
            return;
        }

        double weight = Utils.parseDoubleSafe(etWeight.getText().toString());
        double distance = Utils.parseDoubleSafe(etDistance.getText().toString());
        double volume = Utils.parseDoubleSafe(etVolume.getText().toString()); // Volumen editable
        String code = generateShipmentCode();

        Map<String, Object> body = new HashMap<>();
        body.put("user", userId);
        body.put("shipment_code", code);
        body.put("object_desc", etObject.getText().toString().trim());
        body.put("receiver_address", etAddress.getText().toString().trim());
        body.put("weight_kg", weight);
        body.put("distance_km", distance);
        body.put("volume_m3", volume);
        body.put("status", "CREADO");

        apiService.createShipment(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Utils.toastLong(ShipmentFormActivity.this,
                            "✅ Envío creado.\nCódigo: " + code + "\nVolumen: " + volume + " m³");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Sin cuerpo de error";
                        Utils.toastLong(ShipmentFormActivity.this,
                                "❌ Error al crear el envío.\nHTTP " + response.code() + "\n" + errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.toast(ShipmentFormActivity.this, "❌ Error desconocido al leer respuesta.");
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Utils.toast(ShipmentFormActivity.this, "Error de red: " + t.getMessage());
            }
        });
    }
}