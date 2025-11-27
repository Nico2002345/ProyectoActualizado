package com.example.ingsoftcalvoproy.activities;

import android.content.Intent;
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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShipmentListActivity extends AppCompatActivity {

    private ListView lvUserShipments, lvAllShipments;
    private Button btnAddShipment, btnDeleteShipment;

    private ArrayList<String> shipmentData = new ArrayList<>();
    private ArrayList<Integer> shipmentIds = new ArrayList<>();
    private Integer selectedShipmentId = null;

    private ApiService api;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment_list);

        userId = getIntent().getIntExtra("USER_ID", -1);

        lvUserShipments = findViewById(R.id.lvShipments);
        lvAllShipments = findViewById(R.id.lvTodosRemote);
        btnAddShipment = findViewById(R.id.btnAddShipment);
        btnDeleteShipment = findViewById(R.id.btnDeleteShipment);

        api = ApiClient.getClient().create(ApiService.class);

        loadUserShipments();
        loadAllShipments();

        // Abrir formulario para agregar
        btnAddShipment.setOnClickListener(v -> {
            Intent i = new Intent(this, ShipmentFormActivity.class);
            startActivity(i);
        });

        // Selección de envío
        lvUserShipments.setOnItemClickListener((parent, view, position, id) -> {
            selectedShipmentId = shipmentIds.get(position);
            Utils.toast(this, "✅ Seleccionaste envío ID: " + selectedShipmentId);
        });

        // Eliminar envío seleccionado
        btnDeleteShipment.setOnClickListener(v -> {
            if (selectedShipmentId == null) {
                Utils.toast(this, "⚠️ Selecciona un envío primero.");
                return;
            }
            deleteShipment(selectedShipmentId);
        });
    }

    private void loadUserShipments() {
        Call<List<ShipmentDTO>> call = api.getShipmentsByUser(userId);

        call.enqueue(new Callback<List<ShipmentDTO>>() {
            @Override
            public void onResponse(Call<List<ShipmentDTO>> call, Response<List<ShipmentDTO>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Utils.toast(ShipmentListActivity.this, "No hay envíos del usuario.");
                    return;
                }

                shipmentData.clear();
                shipmentIds.clear();

                for (ShipmentDTO s : response.body()) {
                    String code = s.getShipmentCode() != null ? s.getShipmentCode() : "Sin código";
                    String status = s.getStatus() != null ? s.getStatus() : "Desconocido";
                    String address = s.getReceiverAddress() != null ? s.getReceiverAddress() : "Sin dirección";
                    double weight = s.getWeightKg();
                    double volume = s.getVolumeM3();
                    double distance = s.getDistanceKm();

                    shipmentData.add(
                            "Código: " + code +
                                    "\nEstado: " + status +
                                    "\nDestino: " + address +
                                    "\nPeso: " + weight + " kg | Vol: " + volume + " m³ | Dist: " + distance + " km"
                    );
                    shipmentIds.add(s.getId());
                }

                lvUserShipments.setAdapter(new ArrayAdapter<>(ShipmentListActivity.this,
                        android.R.layout.simple_list_item_1, shipmentData));
            }

            @Override
            public void onFailure(Call<List<ShipmentDTO>> call, Throwable t) {
                Utils.toast(ShipmentListActivity.this, "Error cargando envíos: " + t.getMessage());
            }
        });
    }

    private void loadAllShipments() {
        Call<List<ShipmentDTO>> call = api.getAllShipmentsFromAPI();

        call.enqueue(new Callback<List<ShipmentDTO>>() {
            @Override
            public void onResponse(Call<List<ShipmentDTO>> call, Response<List<ShipmentDTO>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                List<String> data = new ArrayList<>();
                for (ShipmentDTO s : response.body()) {
                    String code = s.getShipmentCode() != null ? s.getShipmentCode() : "Sin código";
                    String status = s.getStatus() != null ? s.getStatus() : "Desconocido";
                    String address = s.getReceiverAddress() != null ? s.getReceiverAddress() : "Sin dirección";

                    data.add("Código: " + code +
                            "\nEstado: " + status +
                            "\nDestino: " + address);
                }

                lvAllShipments.setAdapter(new ArrayAdapter<>(ShipmentListActivity.this,
                        android.R.layout.simple_list_item_1, data));
            }

            @Override
            public void onFailure(Call<List<ShipmentDTO>> call, Throwable t) {
                Utils.toast(ShipmentListActivity.this, "Error: " + t.getMessage());
            }
        });
    }

    private void deleteShipment(int id) {
        Call<Void> call = api.deleteShipment(id);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Utils.toast(ShipmentListActivity.this, "✅ Envío eliminado.");
                    selectedShipmentId = null;
                    loadUserShipments();
                } else {
                    Utils.toast(ShipmentListActivity.this, "❌ No se pudo eliminar.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Utils.toast(ShipmentListActivity.this, "Error: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserShipments(); // refrescar al volver del formulario
    }
}
