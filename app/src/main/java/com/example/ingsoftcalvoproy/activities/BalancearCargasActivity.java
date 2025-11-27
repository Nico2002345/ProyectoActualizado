package com.example.ingsoftcalvoproy.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
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

public class BalancearCargasActivity extends AppCompatActivity {

    private ListView lvShipments;
    private TextView tvTitle;
    private ApiService api;
    private final List<ShipmentDTO> shipmentsList = new ArrayList<>();
    private final List<Integer> repartidorIds = new ArrayList<>();
    private final List<String> repartidorNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balancear_cargas);

        api = ApiClient.getClient().create(ApiService.class);

        lvShipments = findViewById(R.id.lvShipments);
        tvTitle = findViewById(R.id.tvTitle);

        setTitle("Balancear Cargas");

        loadRepartidores();
        loadAsignados();

        // Al hacer clic en un envío, mostrar dialog para reasignar
        lvShipments.setOnItemClickListener((parent, view, position, id) -> {
            if (position < shipmentsList.size()) {
                mostrarDialogReasignar(shipmentsList.get(position));
            }
        });
    }

    private void loadRepartidores() {
        api.getUsers().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                repartidorIds.clear();
                repartidorNames.clear();

                for (Map<String, Object> user : response.body()) {
                    String role = String.valueOf(user.get("role"));
                    if ("REPARTIDOR".equalsIgnoreCase(role)) {
                        repartidorNames.add(String.valueOf(user.get("name")));
                        Object idObj = user.get("id");
                        if (idObj instanceof Number) {
                            repartidorIds.add(((Number) idObj).intValue());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Log.e("BALANCEAR", "Error cargando repartidores: " + t.getMessage());
            }
        });
    }

    private void loadAsignados() {
        api.getAssignedShipmentsWithRepartidor().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvTitle.setText("No hay envíos ASIGNADOS");
                    return;
                }

                shipmentsList.clear();
                List<String> displayList = new ArrayList<>();

                // Convertir Map a ShipmentDTO y mostrar con nombre del repartidor
                for (Map<String, Object> data : response.body()) {
                    ShipmentDTO s = new ShipmentDTO(
                            ((Number) data.get("id")).intValue(),
                            (String) data.get("shipment_code"),
                            (String) data.get("object_desc"),
                            (String) data.get("receiver_address"),
                            ((Number) data.get("weight_kg")).doubleValue(),
                            ((Number) data.get("volume_m3")).doubleValue(),
                            ((Number) data.get("distance_km")).doubleValue(),
                            (String) data.get("status")
                    );
                    shipmentsList.add(s);

                    String repartidorName = (String) data.get("assigned_user_name");
                    String info = String.format(
                            "📦 %s\n👤 Repartidor: %s\nDestino: %s\nPeso: %.2f kg",
                            s.getShipmentCode(),
                            repartidorName != null ? repartidorName : "Sin asignar",
                            s.getReceiverAddress(),
                            s.getWeightKg()
                    );
                    displayList.add(info);
                }

                if (shipmentsList.isEmpty()) {
                    tvTitle.setText("No hay envíos ASIGNADOS para balancear");
                } else {
                    tvTitle.setText("Envíos ASIGNADOS (" + shipmentsList.size() + ") - Toca para reasignar:");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        BalancearCargasActivity.this,
                        android.R.layout.simple_list_item_1,
                        displayList
                );
                lvShipments.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Utils.toast(BalancearCargasActivity.this, "Error cargando envíos: " + t.getMessage());
            }
        });
    }

    private void mostrarDialogReasignar(ShipmentDTO shipment) {
        if (repartidorNames.isEmpty()) {
            Utils.toast(this, "No hay repartidores disponibles");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reasignar_repartidor, null);
        builder.setView(dialogView);

        TextView tvShipmentInfo = dialogView.findViewById(R.id.tvShipmentInfo);
        Spinner spRepartidores = dialogView.findViewById(R.id.spRepartidores);
        Button btnReasignar = dialogView.findViewById(R.id.btnReasignar);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        tvShipmentInfo.setText("Reasignar envío: " + shipment.getShipmentCode());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                repartidorNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRepartidores.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        btnReasignar.setOnClickListener(v -> {
            int pos = spRepartidores.getSelectedItemPosition();
            if (pos >= 0 && pos < repartidorIds.size()) {
                int newRepartidorId = repartidorIds.get(pos);
                reasignarEnvio(shipment.getId(), newRepartidorId);
                dialog.dismiss();
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void reasignarEnvio(int shipmentId, int newRepartidorId) {
        Map<String, Object> body = new HashMap<>();
        body.put("assigned_user_id", newRepartidorId);

        api.reassignShipmentRepartidor(shipmentId, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Utils.toast(BalancearCargasActivity.this, "✅ Envío reasignado correctamente");
                    loadAsignados(); // Recargar la lista
                } else {
                    Utils.toast(BalancearCargasActivity.this, "Error reasignando envío");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Utils.toast(BalancearCargasActivity.this, "Error: " + t.getMessage());
            }
        });
    }
}