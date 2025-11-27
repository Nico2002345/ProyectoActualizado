package com.example.ingsoftcalvoproy.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
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
 * Pantalla que muestra todas las guías desde la API y permite editar la distancia.
 */
public class MostrarGuiasActivity extends AppCompatActivity {

    private LinearLayout layoutGuias;
    private Button btnGuardarCambios;
    private ApiService api;

    private final Map<Integer, EditText> distanciaMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_guias);

        layoutGuias = findViewById(R.id.layoutGuias);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);

        api = ApiClient.getClient().create(ApiService.class);

        cargarGuias();

        btnGuardarCambios.setOnClickListener(v -> guardarCambios());
    }

    private void cargarGuias() {
        layoutGuias.removeAllViews();
        distanciaMap.clear();

        api.getGuides().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    Utils.toast(MostrarGuiasActivity.this, "No hay guías disponibles.");
                    return;
                }

                for (Map<String, Object> guide : response.body()) {
                    int guiaId = ((Double) guide.get("id")).intValue();
                    String guideNumber = (String) guide.get("guide_number");
                    String shipmentCode = (String) guide.get("shipment_code");
                    String destino = (String) guide.get("receiver_address");
                    double peso = (Double) guide.get("weight_kg");
                    double volumen = (Double) guide.get("volume_m3");
                    double distancia = guide.get("distance_km") != null ? ((Double) guide.get("distance_km")) : 0;

                    EditText etDistancia = new EditText(MostrarGuiasActivity.this);
                    etDistancia.setText(String.valueOf(distancia));
                    etDistancia.setHint("Distancia km");
                    etDistancia.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

                    distanciaMap.put(guiaId, etDistancia);

                    LinearLayout row = new LinearLayout(MostrarGuiasActivity.this);
                    row.setOrientation(LinearLayout.VERTICAL);
                    row.setPadding(10, 10, 10, 10);

                    android.widget.TextView tvInfo = new android.widget.TextView(MostrarGuiasActivity.this);
                    tvInfo.setText("Guía: " + guideNumber +
                            " | Código: " + shipmentCode +
                            " | Destino: " + destino +
                            " | Peso: " + peso + " kg" +
                            " | Volumen: " + volumen + " m³");

                    row.addView(tvInfo);
                    row.addView(etDistancia);
                    layoutGuias.addView(row);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Utils.toast(MostrarGuiasActivity.this, "Error cargando guías: " + t.getMessage());
            }
        });
    }

    private void guardarCambios() {
        for (Map.Entry<Integer, EditText> entry : distanciaMap.entrySet()) {
            int guiaId = entry.getKey();
            String texto = entry.getValue().getText().toString().trim();
            if (!texto.isEmpty()) {
                try {
                    double distancia = Double.parseDouble(texto);
                    Map<String, Object> body = new HashMap<>();
                    body.put("distance_km", distancia);

                    api.updatePickupStatus(guiaId, body).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                Utils.toast(MostrarGuiasActivity.this, "Distancia actualizada para guía ID " + guiaId);
                            } else {
                                Utils.toast(MostrarGuiasActivity.this, "Error actualizando guía ID " + guiaId);
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Utils.toast(MostrarGuiasActivity.this, "Error API guía ID " + guiaId + ": " + t.getMessage());
                        }
                    });
                } catch (NumberFormatException e) {
                    Utils.toast(this, "Valor inválido para la guía ID " + guiaId);
                }
            }
        }
    }
}
