package com.example.ingsoftcalvoproy.activities;

import android.content.ContentValues;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;
import com.example.ingsoftcalvoproy.utils.Utils;

/**
 * Formulario para crear una nueva solicitud de recogida (pickup).
 * Usado por el USUARIO para solicitar una recogida.
 */
public class PickupFormActivity extends AppCompatActivity {

    private Db db;
    private EditText etAddress, etDate, etWeight, etVolume, etEmail;
    private Button btnSavePickup;

    // 🔹 Simulación temporal de usuario (cuando se implemente login, se pasará desde sesión)
    private int currentUserId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_form);

        db = new Db(this);

        // === Referencias de campos ===
        etAddress = findViewById(R.id.etAddress);
        etDate = findViewById(R.id.etDate);
        etWeight = findViewById(R.id.etWeight);
        etVolume = findViewById(R.id.etVolume);
        etEmail = findViewById(R.id.etEmail);
        btnSavePickup = findViewById(R.id.btnSavePickup);

        btnSavePickup.setOnClickListener(v -> savePickup());
    }

    /**
     * Guarda la solicitud de recogida en la base de datos
     * y envía un comprobante por correo.
     */
    private void savePickup() {
        String address = etAddress.getText().toString();
        String date = etDate.getText().toString();
        String email = etEmail.getText().toString();

        // === Validaciones básicas ===
        if (Utils.isEmpty(address) || Utils.isEmpty(date)) {
            Utils.toast(this, "Por favor completa la dirección y la fecha.");
            return;
        }

        double weight = Utils.parseDoubleSafe(etWeight.getText().toString());
        double volume = Utils.parseDoubleSafe(etVolume.getText().toString());

        // === Crear código de recogida ===
        String pickupCode = "PKP-" + Utils.generateShipmentCode().substring(4); // Ej: PKP-20251023-1234

        // === Insertar en la base de datos ===
        ContentValues cv = new ContentValues();
        cv.put("user_id", currentUserId);
        cv.put("address", address);
        cv.put("scheduled_at", date);
        cv.put("weight_kg", weight);
        cv.put("volume_m3", volume);
        cv.put("status", "PENDIENTE");
        long id = db.insert("pickups", cv);

        if (id > 0) {
            Utils.toastLong(this, "✅ Recogida registrada con código: " + pickupCode);

            // === Enviar comprobante de solicitud (si hay email) ===
            if (!Utils.isEmpty(email) && Utils.isValidEmail(email)) {
                String body = "📦 Comprobante de Solicitud de Recogida\n" +
                        "Código: " + pickupCode + "\n" +
                        "Dirección: " + address + "\n" +
                        "Fecha programada: " + date + "\n" +
                        "Peso estimado: " + weight + " kg\n" +
                        "Volumen estimado: " + volume + " m³\n" +
                        "Estado actual: PENDIENTE\n" +
                        "Fecha de solicitud: " + Utils.now();

                Utils.sendEmail(this, email, "Comprobante de Recogida " + pickupCode, body);
            }

            finish();
        } else {
            Utils.toast(this, "❌ Error al guardar la recogida.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.closeDB();
    }
}

