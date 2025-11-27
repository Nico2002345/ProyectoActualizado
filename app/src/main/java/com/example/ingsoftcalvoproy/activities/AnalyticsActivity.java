package com.example.ingsoftcalvoproy.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.network.ApiService;
import com.example.ingsoftcalvoproy.network.ApiClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalyticsActivity extends AppCompatActivity {

    private ApiService api;

    private TextView tvCountGuides;
    private TextView tvAvgWeight, tvAvgDist;
    private TextView tvMinWeight, tvMinDist;
    private TextView tvMaxWeight, tvMaxDist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        // ✅ Inicializar API correctamente usando ApiClient
        api = ApiClient.getClient().create(ApiService.class);

// ✅ Enlazar vistas con el XML
        tvCountGuides   = findViewById(R.id.tvCountGuides);

        tvAvgWeight     = findViewById(R.id.tvAvgWeight);
        tvAvgDist       = findViewById(R.id.tvAvgDist);

        tvMinWeight     = findViewById(R.id.tvMinWeight);
        tvMinDist       = findViewById(R.id.tvMinDist);

        tvMaxWeight     = findViewById(R.id.tvMaxWeight);
        tvMaxDist       = findViewById(R.id.tvMaxDist);


        loadAnalytics();
    }

    private void loadAnalytics() {
        loadAverages();
        loadMinimums();
        loadMaximums();
        loadCountByStatus();
    }

    private void loadAverages() {
        api.getAverages().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    tvAvgWeight.setText("Peso promedio: " + data.get("avg_weight") + " kg");
                    tvAvgDist.setText("Distancia promedio: " + data.get("avg_distance") + " km");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AnalyticsActivity.this, "Error cargando promedios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMinimums() {
        api.getMin().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    tvMinWeight.setText("Peso mínimo: " + data.get("min_weight") + " kg");
                    tvMinDist.setText("Distancia mínima: " + data.get("min_distance") + " km");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AnalyticsActivity.this, "Error cargando mínimos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMaximums() {
        api.getMax().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    tvMaxWeight.setText("Peso máximo: " + data.get("max_weight") + " kg");
                    tvMaxDist.setText("Distancia máxima: " + data.get("max_distance") + " km");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AnalyticsActivity.this, "Error cargando máximos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCountByStatus() {
        api.getCountByStatus("ENTREGADO").enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvCountGuides.setText("Guías entregadas: " + response.body().get("count"));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AnalyticsActivity.this, "Error cargando conteo", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
