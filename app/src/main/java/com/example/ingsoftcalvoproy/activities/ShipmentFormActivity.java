package com.example.ingsoftcalvoproy.activities;

import android.content.ContentValues;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Formulario para crear o editar envíos.
 */
public class ShipmentFormActivity extends AppCompatActivity {

    private Db db;

    // Campos del formulario
    private EditText etObject, etHeight, etWidth, etLength, etWeight;
    private EditText etSenderName, etSenderContact;
    private EditText etReceiverName, etReceiverContact, etReceiverAddress, etDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment_form);

        db = new Db(this);

        // Inicializar campos del XML
        etObject = findViewById(R.id.etObject);
        etHeight = findViewById(R.id.etHeight);
        etWidth = findViewById(R.id.etWidth);
        etLength = findViewById(R.id.etLength);
        etWeight = findViewById(R.id.etWeight);
        etSenderName = findViewById(R.id.etSenderName);
        etSenderContact = findViewById(R.id.etSenderContact);
        etReceiverName = findViewById(R.id.etReceiverName);
        etReceiverContact = findViewById(R.id.etReceiverContact);
        etReceiverAddress = findViewById(R.id.etReceiverAddress);
        etDistance = findViewById(R.id.etDistance);

        findViewById(R.id.btnSaveShipment).setOnClickListener(v -> saveShipment());
    }

    private void saveShipment() {
        try {
            // Validar campos requeridos
            if (etObject.getText().toString().isEmpty() ||
                    etSenderName.getText().toString().isEmpty() ||
                    etReceiverName.getText().toString().isEmpty() ||
                    etReceiverAddress.getText().toString().isEmpty()) {
                Toast.makeText(this, "Por favor completa los campos obligatorios.", Toast.LENGTH_SHORT).show();
                return;
            }

            String code = generateShipmentCode();

            // Convertir los valores numéricos (si están vacíos, asigna 0)
            double height = parseDoubleSafe(etHeight.getText().toString());
            double width = parseDoubleSafe(etWidth.getText().toString());
            double length = parseDoubleSafe(etLength.getText().toString());
            double weight = parseDoubleSafe(etWeight.getText().toString());
            double distance = parseDoubleSafe(etDistance.getText().toString());

            ContentValues cv = new ContentValues();
            cv.put("shipment_code", code);
            cv.put("object_desc", etObject.getText().toString());
            cv.put("height_cm", height);
            cv.put("width_cm", width);
            cv.put("length_cm", length);
            cv.put("weight_kg", weight);
            cv.put("sender_name", etSenderName.getText().toString());
            cv.put("sender_contact", etSenderContact.getText().toString());
            cv.put("receiver_name", etReceiverName.getText().toString());
            cv.put("receiver_contact", etReceiverContact.getText().toString());
            cv.put("receiver_address", etReceiverAddress.getText().toString());
            cv.put("distance_km", distance);

            long id = db.insert("shipments", cv);

            if (id > 0) {
                ContentValues ev = new ContentValues();
                ev.put("shipment_id", id);
                ev.put("status", "CREADO");
                ev.put("location", "Origen");
                db.insert("tracking_events", ev);

                Toast.makeText(this, "✅ Envío creado con código: " + code, Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "❌ Error al guardar envío.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private String generateShipmentCode() {
        String date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        int rnd = (int) (Math.random() * 9000) + 1000;
        return "ENV-" + date + "-" + rnd;
    }

    // Evita errores si un campo numérico está vacío
    private double parseDoubleSafe(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
