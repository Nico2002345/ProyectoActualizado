package com.example.ingsoftcalvoproy.activities;

import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;
import com.example.ingsoftcalvoproy.utils.Utils;

/**
 * Formulario para crear envíos (DB actual: con cálculo de volumen y sin envío de correo).
 */
public class ShipmentFormActivity extends AppCompatActivity {

    private Db db;
    private EditText etObject, etAddress, etWeight, etDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment_form);

        db = new Db(this);

        etObject   = findViewById(R.id.etObject);
        etAddress  = findViewById(R.id.etAddress);
        etWeight   = findViewById(R.id.etWeight);
        etDistance = findViewById(R.id.etDistance);

        findViewById(R.id.btnSaveShipment).setOnClickListener(v -> saveShipment());
    }

    private void saveShipment() {
        // Validaciones básicas
        if (Utils.isEmpty(etObject.getText().toString())
                || Utils.isEmpty(etAddress.getText().toString())
                || Utils.isEmpty(etWeight.getText().toString())
                || Utils.isEmpty(etDistance.getText().toString())) {
            Utils.toast(this, "Completa todos los campos obligatorios.");
            return;
        }

        double weight   = Utils.parseDoubleSafe(etWeight.getText().toString());
        double distance = Utils.parseDoubleSafe(etDistance.getText().toString());
        double volume   = weight * distance; // 🔹 Cálculo del volumen
        String code     = Utils.generateShipmentCode();

        ContentValues cv = new ContentValues();
        cv.put("shipment_code", code);
        cv.put("object_desc", etObject.getText().toString().trim());
        cv.put("receiver_address", etAddress.getText().toString().trim());
        cv.put("weight_kg", weight);
        cv.put("distance_km", distance);
        cv.put("volume_m3", volume); // 🔹 Se guarda el volumen en la DB
        cv.put("status", "CREADO");

        long shipmentId = -1;
        try {
            shipmentId = db.insert("shipments", cv);
            Log.d("DB_DEBUG", "Insertando envío: " + cv);
            Log.d("DB_DEBUG", "Resultado insert: " + shipmentId);
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error al insertar envío", e);
            Utils.toast(this, "❌ Error en la base de datos: " + e.getMessage());
            return;
        }

        if (shipmentId <= 0) {
            Utils.toast(this, "❌ Error al guardar el envío (verifica la tabla shipments).");
            return;
        }

        try {
            ContentValues ev = new ContentValues();
            ev.put("shipment_id", shipmentId);
            ev.put("status", "CREADO");
            ev.put("location", "Origen");
            db.insert("tracking_events", ev);
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error al insertar evento de tracking", e);
        }

        Utils.toastLong(this, "✅ Envío creado.\nCódigo: " + code + "\nVolumen: " + volume + " m³");

        // Indicamos que la creación fue exitosa y cerramos la activity
        setResult(RESULT_OK);
        finish();
    }

    // 🔹 Este método se ejecuta cada vez que la Activity vuelve al frente
    @Override
    protected void onResume() {
        super.onResume();
        loadShipments(); // refresca los datos
    }

    private void loadShipments() {
        // 🧩 Aquí puedes agregar la lógica para recargar los envíos si es necesario
        Log.d("SHIPMENT_FORM", "Recargando envíos...");
        // Normalmente, esta función estaría en ShipmentListActivity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.closeDB();
    }
}
