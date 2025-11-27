package com.example.ingsoftcalvoproy.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.network.ApiClient;
import com.example.ingsoftcalvoproy.network.ApiService;
import com.example.ingsoftcalvoproy.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrucksListActivity extends AppCompatActivity {

    private ListView lvTrucks;
    private ArrayList<String> data = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trucks_list);

        lvTrucks = findViewById(R.id.lvTrucks);
        apiService = ApiClient.getClient().create(ApiService.class);

        loadTrucksFromApi();
    }

    /**
     * Carga los camiones desde la API y los muestra en la lista.
     */
    private void loadTrucksFromApi() {
        data.clear();

        Call<List<Map<String, Object>>> call = apiService.getTrucks();
        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    data.add("🚫 Error al obtener los camiones de la API.");
                    lvTrucks.setAdapter(new ArrayAdapter<>(TrucksListActivity.this,
                            android.R.layout.simple_list_item_1, data));
                    return;
                }

                List<Map<String, Object>> trucks = response.body();
                if (trucks.isEmpty()) {
                    data.add("🚫 No hay camiones registrados en el sistema.");
                } else {
                    for (Map<String, Object> truck : trucks) {
                        String plate = truck.get("plate").toString();
                        double capacity = Double.parseDouble(truck.get("capacity_kg").toString());
                        boolean active = Boolean.parseBoolean(truck.get("active").toString());
                        String status = active ? "Activo ✅" : "Inactivo ❌";

                        data.add("🚛 Placa: " + plate +
                                "\nCapacidad: " + capacity + " kg" +
                                "\nEstado: " + status);
                    }
                }

                lvTrucks.setAdapter(new ArrayAdapter<>(TrucksListActivity.this,
                        android.R.layout.simple_list_item_1, data));
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                data.add("🚫 Error de red: " + t.getMessage());
                lvTrucks.setAdapter(new ArrayAdapter<>(TrucksListActivity.this,
                        android.R.layout.simple_list_item_1, data));
            }
        });
    }
}
