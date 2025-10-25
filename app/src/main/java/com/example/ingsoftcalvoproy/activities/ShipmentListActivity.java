package com.example.ingsoftcalvoproy.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;
import com.example.ingsoftcalvoproy.utils.Utils;

import java.util.ArrayList;

/**
 * Lista de todos los envíos registrados con detalles principales.
 */
public class ShipmentListActivity extends AppCompatActivity {

    private Db db;
    private ListView lvShipments;
    private ArrayList<String> data = new ArrayList<>();
    private ArrayList<String> shipmentCodes = new ArrayList<>(); // 🔹 Guardar códigos
    private Button btnAddShipment, btnDeleteShipment; // 🔹 Nuevo botón
    private String selectedCode = null; // 🔹 Envío seleccionado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment_list);

        db = new Db(this);
        lvShipments = findViewById(R.id.lvShipments);
        btnAddShipment = findViewById(R.id.btnAddShipment);
        btnDeleteShipment = findViewById(R.id.btnDeleteShipment); // 🔹 Referencia al nuevo botón

        loadShipments();

        // 🔹 Acción del botón "Nuevo Envío"
        btnAddShipment.setOnClickListener(v -> {
            Intent i = new Intent(this, ShipmentFormActivity.class);
            startActivity(i);
        });

        // 🔹 Seleccionar envío al tocarlo
        lvShipments.setOnItemClickListener((parent, view, position, id) -> {
            selectedCode = shipmentCodes.get(position);
            Utils.toast(this, "Seleccionaste el envío: " + selectedCode);
        });

        // 🔹 Acción del botón "Eliminar Envío"
        btnDeleteShipment.setOnClickListener(v -> {
            if (selectedCode == null) {
                Utils.toast(this, "⚠️ Primero selecciona un envío para eliminar.");
                return;
            }

            int deleted = db.delete("shipments", "shipment_code = ?", new String[]{selectedCode});
            if (deleted > 0) {
                Utils.toast(this, "🗑️ Envío eliminado correctamente.");
                selectedCode = null;
                loadShipments();
            } else {
                Utils.toast(this, "❌ No se pudo eliminar el envío.");
            }
        });
    }

    private void loadShipments() {
        data.clear();
        shipmentCodes.clear();

        Cursor c = db.raw("""
                SELECT shipment_code, status, receiver_address, 
                       weight_kg, volume_m3, distance_km 
                FROM shipments 
                ORDER BY id DESC
                """, null);

        while (c.moveToNext()) {
            String code = c.getString(0);
            String status = c.getString(1);
            String address = c.getString(2);
            double weight = c.getDouble(3);
            double volume = c.getDouble(4);
            double distance = c.getDouble(5);

            String wClass = Utils.classifyWeight(weight);
            String vClass = Utils.classifyVolume(volume);
            String dClass = Utils.classifyDistance(distance);
            String statusText = Utils.formatStatus(status);

            data.add("Código: " + code + "\n"
                    + statusText + "\n"
                    + "Peso: " + weight + " kg (" + wClass + ")"
                    + " | Vol: " + volume + " m³ (" + vClass + ")\n"
                    + "Distancia: " + distance + " km (" + dClass + ")\n"
                    + "Destino: " + address);

            shipmentCodes.add(code); // 🔹 Guardar el código correspondiente
        }

        c.close();
        lvShipments.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadShipments(); // 🔹 Refresca la lista al volver del formulario
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.closeDB();
    }
}
