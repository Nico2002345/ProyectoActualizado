package com.example.ingsoftcalvoproy.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;

import java.util.ArrayList;

/**
 * Lista de todos los envíos registrados.
 */
public class ShipmentListActivity extends AppCompatActivity {

    private Db db;
    private ListView lvShipments;
    private ArrayList<String> data = new ArrayList<>();
    private ArrayList<Integer> shipmentIds = new ArrayList<>(); // 👈 Guardamos los IDs reales de cada envío

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment_list);

        db = new Db(this);
        lvShipments = findViewById(R.id.lvShipments);

        // ✅ Botón para agregar nuevo envío
        Button btnAddShipment = findViewById(R.id.btnAddShipment);
        btnAddShipment.setOnClickListener(v -> {
            Intent intent = new Intent(this, ShipmentFormActivity.class);
            startActivity(intent);
        });

        // ✅ Cargar lista
        loadShipments();

        // ✅ Al tocar un envío, abrimos el formulario en modo edición
        lvShipments.setOnItemClickListener((parent, view, position, id) -> {
            int shipmentId = shipmentIds.get(position); // obtener el ID del envío seleccionado

            Intent intent = new Intent(this, ShipmentFormActivity.class);
            intent.putExtra("SHIPMENT_ID", shipmentId); // enviamos el ID al formulario
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // recargamos la lista al volver del formulario
        loadShipments();
    }

    private void loadShipments() {
        data.clear();
        shipmentIds.clear();

        Cursor c = db.raw("SELECT id, shipment_code, status, receiver_address FROM shipments ORDER BY id DESC", null);
        while (c.moveToNext()) {
            shipmentIds.add(c.getInt(0)); // ID
            data.add(c.getString(1) + " - " + c.getString(2) + " (" + c.getString(3) + ")");
        }
        c.close();

        lvShipments.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data));
    }
}
