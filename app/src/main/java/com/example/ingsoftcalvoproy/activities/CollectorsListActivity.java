package com.example.ingsoftcalvoproy.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.network.ApiClient;
import com.example.ingsoftcalvoproy.network.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CollectorsListActivity extends AppCompatActivity {

    private ListView lvCollectors;
    private ApiService api;

    private ArrayList<String> data = new ArrayList<>();
    private ArrayList<Integer> ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collectors_list);

        lvCollectors = findViewById(R.id.lvCollectors);
        api = ApiClient.getClient().create(ApiService.class);

        loadCollectors();
    }

    private void loadCollectors() {
        api.getCollectors().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(CollectorsListActivity.this, "Error cargando recolectores", Toast.LENGTH_SHORT).show();
                    return;
                }

                data.clear();
                ids.clear();

                for (Map<String, Object> c : response.body()) {

                    int id = (int) c.get("id");
                    String name = (String) c.get("name");
                    String phone = (String) c.get("phone");
                    String email = (String) c.get("email");
                    boolean active = Boolean.TRUE.equals(c.get("active"));

                    String status = active ? "🟢 Activo" : "🔴 Inactivo";

                    data.add(
                            "ID: " + id +
                                    "\nNombre: " + name +
                                    "\nTeléfono: " + (phone != null ? phone : "N/D") +
                                    "\nEmail: " + (email != null ? email : "N/D") +
                                    "\nEstado: " + status
                    );

                    ids.add(id);
                }

                lvCollectors.setAdapter(
                        new ArrayAdapter<>(CollectorsListActivity.this, android.R.layout.simple_list_item_1, data)
                );
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(CollectorsListActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
