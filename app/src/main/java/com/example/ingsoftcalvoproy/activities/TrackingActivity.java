package com.example.ingsoftcalvoproy.activities;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.network.ApiClient;
import com.example.ingsoftcalvoproy.network.ApiService;
import com.example.ingsoftcalvoproy.utils.Utils;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingActivity extends AppCompatActivity {

    private EditText etShipment;
    private TextView tvEvents;
    private Button btnSearch, btnViewMap;

    private ApiService apiService;

    // Latitud y longitud retornadas por el backend
    private double shipmentLat = 0;
    private double shipmentLng = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        etShipment = findViewById(R.id.etShipment);
        tvEvents = findViewById(R.id.tvEvents);
        btnSearch = findViewById(R.id.btnSearch);
        btnViewMap = findViewById(R.id.btnViewMap);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnSearch.setOnClickListener(v -> fetchTrackingEvents());
        btnViewMap.setOnClickListener(v -> openMap());
    }

    /**
     * Paso 1: Obtener información del envío por código
     * Paso 2: Obtener sus eventos de tracking
     */
    private void fetchTrackingEvents() {
        String shipmentCode = etShipment.getText().toString().trim();

        if (shipmentCode.isEmpty()) {
            Utils.toast(this, "Por favor ingresa un código de envío.");
            return;
        }

        // Llamado para obtener la información del envío
        Call<Map<String, Object>> shipmentCall = apiService.getShipmentByCode(shipmentCode);

        shipmentCall.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Utils.toast(TrackingActivity.this, "No se encontró el envío.");
                    return;
                }

                Map<String, Object> shipment = response.body();

                // Guardar coordenadas
                if (shipment.get("latitude") != null)
                    shipmentLat = ((Number) shipment.get("latitude")).doubleValue();

                if (shipment.get("longitude") != null)
                    shipmentLng = ((Number) shipment.get("longitude")).doubleValue();

                // Obtener ID para los eventos
                int shipmentId = ((Number) shipment.get("id")).intValue();

                fetchEvents(shipmentId);
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Utils.toast(TrackingActivity.this, "Error al obtener el envío: " + t.getMessage());
            }
        });
    }

    /**
     * Obtiene el historial de tracking
     */
    private void fetchEvents(int shipmentId) {
        Call<List<Map<String, Object>>> eventsCall = apiService.getTrackingEvents(shipmentId);

        eventsCall.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Utils.toast(TrackingActivity.this, "No se pudieron obtener los eventos.");
                    return;
                }

                List<Map<String, Object>> events = response.body();

                if (events.isEmpty()) {
                    tvEvents.setText("No hay eventos registrados para este envío.");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                int counter = 0;

                for (Map<String, Object> event : events) {
                    counter++;

                    String status = String.valueOf(event.get("status"));
                    String location = String.valueOf(event.get("location"));
                    String time = String.valueOf(event.get("event_time"));

                    sb.append(counter)
                            .append(". ").append(status)
                            .append(" - ").append(location)
                            .append("\n🕒 ").append(time)
                            .append("\n\n");
                }

                tvEvents.setText(sb.toString());
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Utils.toast(TrackingActivity.this, "Error al obtener eventos: " + t.getMessage());
            }
        });
    }

    /**
     * Abre Google Maps con la ubicación del paquete
     */
    private void openMap() {
        if (shipmentLat == 0 && shipmentLng == 0) {
            Utils.toast(this, "No se ha obtenido la ubicación del envío.");
            return;
        }

        Uri gmmIntentUri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1" +
                        "&destination=" + shipmentLat + "," + shipmentLng +
                        "&travelmode=driving"
        );

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(mapIntent);
        } catch (Exception e) {
            Utils.toast(this, "No se pudo abrir Google Maps.");
        }
    }
}
