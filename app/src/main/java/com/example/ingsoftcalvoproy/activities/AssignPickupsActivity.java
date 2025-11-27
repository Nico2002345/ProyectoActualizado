package com.example.ingsoftcalvoproy.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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

public class AssignPickupsActivity extends AppCompatActivity {

    private ListView lvPickups;
    private TextView tvPickupsTitle;
    private ApiService api;

    private final List<Map<String, Object>> pickupsList = new ArrayList<>();
    private final List<Integer> collectorIds = new ArrayList<>();
    private final List<String> collectorNames = new ArrayList<>();
    private final List<Integer> truckIds = new ArrayList<>();
    private final List<String> truckNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_pickups);

        api = ApiClient.getClient().create(ApiService.class);

        lvPickups = findViewById(R.id.lvPickups);
        tvPickupsTitle = findViewById(R.id.tvPickupsTitle);

        setTitle("Asignar Recolector");

        // Cargar collectors, trucks y pickups
        loadCollectors();
        loadTrucks();
        loadPendingPickups();

        // Al hacer clic en un pickup, mostrar dialog para asignar
        lvPickups.setOnItemClickListener((parent, view, position, id) -> {
            if (position < pickupsList.size()) {
                showAssignDialog(pickupsList.get(position));
            }
        });
    }

    private void loadCollectors() {
        // Cargar usuarios con role RECOLECTOR
        api.getUsers().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                collectorIds.clear();
                collectorNames.clear();

                // Agregar opción "Sin asignar" al inicio
                collectorNames.add("Sin asignar");
                collectorIds.add(-1);

                for (Map<String, Object> user : response.body()) {
                    String role = String.valueOf(user.get("role"));

                    // Filtrar solo usuarios RECOLECTOR
                    if ("RECOLECTOR".equalsIgnoreCase(role)) {
                        collectorNames.add(String.valueOf(user.get("name")));
                        Object idObj = user.get("id");
                        if (idObj instanceof Number) {
                            collectorIds.add(((Number) idObj).intValue());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Utils.toast(AssignPickupsActivity.this, "Error cargando recolectores: " + t.getMessage());
            }
        });
    }

    private void loadTrucks() {
        api.getTrucks().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                truckIds.clear();
                truckNames.clear();

                // Agregar opción "Sin asignar" al inicio
                truckNames.add("Sin asignar");
                truckIds.add(-1);

                for (Map<String, Object> truck : response.body()) {
                    Object activeObj = truck.get("active");
                    boolean active = activeObj instanceof Boolean ? (Boolean) activeObj : true;

                    if (active) {
                        String plate = String.valueOf(truck.get("plate"));
                        double capacity = truck.get("capacity_kg") != null ? ((Number) truck.get("capacity_kg")).doubleValue() : 0;
                        String displayName = plate + " (" + capacity + " kg)";

                        truckNames.add(displayName);
                        Object idObj = truck.get("id");
                        if (idObj instanceof Number) {
                            truckIds.add(((Number) idObj).intValue());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Utils.toast(AssignPickupsActivity.this, "Error cargando camiones: " + t.getMessage());
            }
        });
    }

    private void loadPendingPickups() {
        api.getAllPickupsPending().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvPickupsTitle.setText("No hay pickups pendientes");
                    return;
                }

                pickupsList.clear();
                List<String> displayList = new ArrayList<>();

                for (Map<String, Object> pickup : response.body()) {
                    pickupsList.add(pickup);

                    int pickupId = ((Number) pickup.get("id")).intValue();
                    String address = (String) pickup.get("address");
                    double weight = ((Number) pickup.get("weight_kg")).doubleValue();
                    double volume = ((Number) pickup.get("volume_m3")).doubleValue();
                    String status = (String) pickup.get("status");

                    String info = String.format(
                            "📦 Pickup #%d\n📍 %s\n⚖ %.2f kg | 📦 %.3f m³\n📊 Estado: %s",
                            pickupId,
                            address != null ? address : "Sin dirección",
                            weight,
                            volume,
                            status != null ? status : "PENDIENTE"
                    );
                    displayList.add(info);
                }

                if (pickupsList.isEmpty()) {
                    tvPickupsTitle.setText("No hay pickups pendientes de asignación");
                } else {
                    tvPickupsTitle.setText("Pickups Pendientes (" + pickupsList.size() + ") - Toca para asignar:");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        AssignPickupsActivity.this,
                        android.R.layout.simple_list_item_1,
                        displayList
                );
                lvPickups.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Utils.toast(AssignPickupsActivity.this, "Error cargando pickups: " + t.getMessage());
            }
        });
    }

    private void showAssignDialog(Map<String, Object> pickup) {
        // Verificar que haya datos cargados (al menos la opción "Sin asignar")
        if (collectorNames.size() <= 1 && truckNames.size() <= 1) {
            Utils.toast(this, "No hay recolectores ni camiones disponibles");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_assign_collector, null);
        builder.setView(dialogView);

        TextView tvPickupInfo = dialogView.findViewById(R.id.tvPickupInfo);
        Spinner spCollectors = dialogView.findViewById(R.id.spCollectors);
        Spinner spTrucks = dialogView.findViewById(R.id.spTrucks);
        Button btnAssign = dialogView.findViewById(R.id.btnAssign);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        int pickupId = ((Number) pickup.get("id")).intValue();
        String address = (String) pickup.get("address");

        tvPickupInfo.setText("Asignar Pickup #" + pickupId + "\n📍 " + address);

        // Adapter para recolectores
        ArrayAdapter<String> collectorsAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                collectorNames
        );
        collectorsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCollectors.setAdapter(collectorsAdapter);

        // Adapter para camiones
        ArrayAdapter<String> trucksAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                truckNames
        );
        trucksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTrucks.setAdapter(trucksAdapter);

        AlertDialog dialog = builder.create();

        btnAssign.setOnClickListener(v -> {
            int collectorPos = spCollectors.getSelectedItemPosition();
            int truckPos = spTrucks.getSelectedItemPosition();

            // Obtener IDs seleccionados (-1 si es "Sin asignar")
            int recolectorUserId = collectorIds.get(collectorPos);
            int truckId = truckIds.get(truckPos);

            // Validar que al menos uno esté seleccionado
            if (recolectorUserId == -1 && truckId == -1) {
                Utils.toast(AssignPickupsActivity.this, "Debe seleccionar al menos un recolector o un camión");
                return;
            }

            assignPickup(pickupId, recolectorUserId, truckId);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void assignPickup(int pickupId, int recolectorUserId, int truckId) {
        Map<String, Object> body = new HashMap<>();

        // Solo agregar al body si no es -1 (Sin asignar)
        if (recolectorUserId != -1) {
            body.put("recolector_user_id", recolectorUserId);
        }

        if (truckId != -1) {
            body.put("truck_id", truckId);
        }

        api.assignCollectorAndTruckToPickup(pickupId, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Utils.toast(AssignPickupsActivity.this, "✅ Recolector asignado correctamente");
                    loadPendingPickups(); // Recargar la lista
                } else {
                    Utils.toast(AssignPickupsActivity.this, "Error asignando recolector");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Utils.toast(AssignPickupsActivity.this, "Error: " + t.getMessage());
            }
        });
    }
}