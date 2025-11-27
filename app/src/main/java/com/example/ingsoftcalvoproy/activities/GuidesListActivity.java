package com.example.ingsoftcalvoproy.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
 * Lista de guías de envío usando APIs.
 */
public class GuidesListActivity extends AppCompatActivity {

    private ListView lvGuides;
    private Button btnClassify;
    private ArrayList<String> data = new ArrayList<>();
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guides_list);

        lvGuides = findViewById(R.id.lvGuides);
        btnClassify = findViewById(R.id.btnClassify);
        api = ApiClient.getClient().create(ApiService.class);

        loadGuides();

        btnClassify.setOnClickListener(v -> classifyGuides());
    }

    /** Carga las guías desde la API */
    private void loadGuides() {
        api.getAllShipmentsFromAPI().enqueue(new Callback<List<ShipmentDTO>>() {
            @Override
            public void onResponse(Call<List<ShipmentDTO>> call, Response<List<ShipmentDTO>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Utils.toast(GuidesListActivity.this, "No se pudieron cargar las guías");
                    return;
                }

                data.clear();
                List<ShipmentDTO> shipments = response.body();
                for (ShipmentDTO s : shipments) {
                    String weightClass = Utils.classifyWeight(s.getWeightKg());
                    String volumeClass = Utils.classifyVolume(s.getVolumeM3());
                    String distanceClass = Utils.classifyDistance(s.getDistanceKm());

                    data.add("Envío ID: " + s.getId() +
                            "\nCódigo: " + s.getShipmentCode() +
                            "\nPeso: " + s.getWeightKg() + " kg (" + weightClass + ")" +
                            " | Vol: " + s.getVolumeM3() + " m³ (" + volumeClass + ")" +
                            "\nDistancia: " + s.getDistanceKm() + " km (" + distanceClass + ")" +
                            "\nEstado: " + s.getStatus());
                }

                lvGuides.setAdapter(new ArrayAdapter<>(GuidesListActivity.this,
                        android.R.layout.simple_list_item_1, data));
            }

            @Override
            public void onFailure(Call<List<ShipmentDTO>> call, Throwable t) {
                Utils.toast(GuidesListActivity.this, "Error cargando guías: " + t.getMessage());
            }
        });
    }

    /** Clasifica las guías vía API */
    private void classifyGuides() {
        api.classifyGuides().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Utils.toast(GuidesListActivity.this, "Guías clasificadas correctamente ✅");
                    loadGuides(); // recargar datos actualizados
                } else {
                    Utils.toast(GuidesListActivity.this, "Error clasificando guías");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Utils.toast(GuidesListActivity.this, "Error clasificando guías: " + t.getMessage());
            }
        });
    }
}
