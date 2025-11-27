package com.example.ingsoftcalvoproy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.dto.ShipmentDTO;
import com.example.ingsoftcalvoproy.network.ApiClient;
import com.example.ingsoftcalvoproy.network.ApiService;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConductorRutasActivity extends AppCompatActivity {

    private EditText etTruckPlate, etTruckKg, etTruckM3;
    private Spinner spCamiones, spRepartidores;
    private ListView lvEnvios;
    private Button btnAddTruck, btnAsignar;
    private Button btnLogout;

    private ApiService api;
    private final List<Integer> truckIds = new ArrayList<>();
    private final List<Integer> pickupIds = new ArrayList<>();
    private final List<Integer> repartidorIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conductor_rutas);

        api = ApiClient.getClient().create(ApiService.class);

        etTruckPlate = findViewById(R.id.etTruckPlate);
        etTruckKg = findViewById(R.id.etTruckKg);
        etTruckM3 = findViewById(R.id.etTruckM3);

        spCamiones = findViewById(R.id.spinnerCamiones);
        spRepartidores = findViewById(R.id.spinnerRepartidores);
        lvEnvios = findViewById(R.id.lvEnvios);

        btnAddTruck = findViewById(R.id.btnAddTruck);
        btnAsignar = findViewById(R.id.btnGuardar);
        btnLogout = findViewById(R.id.btnLogout);

        // 🔹 Aquí pongo tu logout EXACTAMENTE igual, pero ahora sí dentro del onCreate()
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(ConductorRutasActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        loadTrucks();
        loadRepartidores();
        loadPickups();

        btnAddTruck.setOnClickListener(v -> addTruck());
        btnAsignar.setOnClickListener(v -> assignRoutes());
    }

    // ========================== CARGAR CAMIONES ==========================
    private void loadTrucks() {
        api.getTrucks().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                Log.d("CONDUCTOR", "Response code: " + response.code());
                Log.d("CONDUCTOR", "Response successful: " + response.isSuccessful());
                Log.d("CONDUCTOR", "Response body null: " + (response.body() == null));

                if (!response.isSuccessful() || response.body() == null) {
                    String errorMsg = "Error al obtener camiones. Code: " + response.code();
                    Toast.makeText(ConductorRutasActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e("CONDUCTOR", errorMsg);
                    return;
                }

                List<String> names = new ArrayList<>();
                truckIds.clear();

                for (Map<String, Object> t : response.body()) {
                    names.add(String.valueOf(t.get("plate")));
                    Object idObj = t.get("id");
                    if (idObj instanceof Number) {
                        truckIds.add(((Number) idObj).intValue());
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(ConductorRutasActivity.this,
                        android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spCamiones.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(ConductorRutasActivity.this, "Error cargando camiones", Toast.LENGTH_SHORT).show();
                Log.e("API", "loadTrucks: " + t.getMessage());
            }
        });
    }

    // ========================== CARGAR REPARTIDORES ==========================
    private void loadRepartidores() {
        api.getUsers().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                Log.d("REPARTIDORES", "Response code: " + response.code());
                Log.d("REPARTIDORES", "Response successful: " + response.isSuccessful());

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ConductorRutasActivity.this, "Error al obtener repartidores", Toast.LENGTH_SHORT).show();
                    Log.e("REPARTIDORES", "Error: code=" + response.code());
                    return;
                }

                Log.d("REPARTIDORES", "Total usuarios recibidos: " + response.body().size());

                List<String> names = new ArrayList<>();
                repartidorIds.clear();

                for (Map<String, Object> user : response.body()) {
                    String role = String.valueOf(user.get("role"));
                    String name = String.valueOf(user.get("name"));
                    Log.d("REPARTIDORES", "Usuario: " + name + ", Rol: " + role);

                    if ("REPARTIDOR".equalsIgnoreCase(role)) {
                        names.add(name);
                        Object idObj = user.get("id");
                        if (idObj instanceof Number) {
                            repartidorIds.add(((Number) idObj).intValue());
                            Log.d("REPARTIDORES", "Repartidor agregado: " + name + " (ID: " + idObj + ")");
                        }
                    }
                }

                Log.d("REPARTIDORES", "Total repartidores encontrados: " + names.size());

                if (names.isEmpty()) {
                    names.add("No hay repartidores disponibles");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(ConductorRutasActivity.this,
                        android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spRepartidores.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(ConductorRutasActivity.this, "Error cargando repartidores", Toast.LENGTH_SHORT).show();
                Log.e("REPARTIDORES", "loadRepartidores failure: " + t.getMessage());
            }
        });
    }

    // ========================== CARGAR PICKUPS PENDIENTES ==========================
    private void loadPickups() {
        api.getPendingPickups().enqueue(new Callback<List<ShipmentDTO>>() {
            @Override
            public void onResponse(Call<List<ShipmentDTO>> call, Response<List<ShipmentDTO>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ConductorRutasActivity.this, "Error al obtener envíos", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<String> addresses = new ArrayList<>();
                pickupIds.clear();

                for (ShipmentDTO p : response.body()) {
                    addresses.add(p.getReceiverAddress());
                    pickupIds.add(p.getId());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(ConductorRutasActivity.this,
                        android.R.layout.simple_list_item_multiple_choice, addresses);

                lvEnvios.setAdapter(adapter);
                lvEnvios.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }

            @Override
            public void onFailure(Call<List<ShipmentDTO>> call, Throwable t) {
                Toast.makeText(ConductorRutasActivity.this, "Error cargando envíos", Toast.LENGTH_SHORT).show();
                Log.e("API", "loadPickups: " + t.getMessage());
            }
        });
    }

    // ========================== AGREGAR CAMIÓN ==========================
    private void addTruck() {
        String plate = etTruckPlate.getText().toString().trim();
        String kg = etTruckKg.getText().toString().trim();
        String m3 = etTruckM3.getText().toString().trim();

        if (plate.isEmpty() || kg.isEmpty() || m3.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos del camión", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("plate", plate);
        body.put("capacity_kg", Double.parseDouble(kg));
        body.put("capacity_m3", Double.parseDouble(m3));
        body.put("active", true);

        api.createTruck(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ConductorRutasActivity.this, "Camión agregado", Toast.LENGTH_SHORT).show();
                    etTruckPlate.setText("");
                    etTruckKg.setText("");
                    etTruckM3.setText("");
                    loadTrucks();
                } else {
                    Toast.makeText(ConductorRutasActivity.this, "Error agregando camión", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ConductorRutasActivity.this, "Error al guardar camión", Toast.LENGTH_SHORT).show();
                Log.e("API", "addTruck: " + t.getMessage());
            }
        });
    }

    // ========================== ASIGNAR RUTAS ==========================
    private void assignRoutes() {
        int posTruck = spCamiones.getSelectedItemPosition();
        if (posTruck < 0) {
            Toast.makeText(this, "Seleccione un camión", Toast.LENGTH_SHORT).show();
            return;
        }

        int posRepartidor = spRepartidores.getSelectedItemPosition();
        if (posRepartidor < 0 || repartidorIds.isEmpty()) {
            Toast.makeText(this, "Seleccione un repartidor", Toast.LENGTH_SHORT).show();
            return;
        }

        int truckId = truckIds.get(posTruck);
        int repartidorId = repartidorIds.get(posRepartidor);

        for (int i = 0; i < lvEnvios.getCount(); i++) {
            if (lvEnvios.isItemChecked(i)) {
                int pickupId = pickupIds.get(i);

                Map<String, Object> body = new HashMap<>();
                body.put("truck_id", truckId);
                body.put("assigned_user_id", repartidorId);

                api.assignShipmentToTruck(pickupId, body).enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) { }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Log.e("API", "assignRoutes: " + t.getMessage());
                    }
                });
            }
        }

        Toast.makeText(this, "Rutas asignadas", Toast.LENGTH_SHORT).show();
        loadPickups();
    }
}
