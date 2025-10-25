package com.example.ingsoftcalvoproy.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;
import com.example.ingsoftcalvoproy.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Pantalla que muestra todas las guías para monitoreo y permite editar la distancia.
 */
public class MostrarGuiasActivity extends AppCompatActivity {

    private LinearLayout layoutGuias;
    private Db db;
    private Button btnGuardarCambios;

    // Map para relacionar id de guía con el EditText de distancia
    private final Map<Integer, EditText> distanciaMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_guias); // Layout debe tener ScrollView + LinearLayout + Botón

        db = new Db(this);

        layoutGuias = findViewById(R.id.layoutGuias);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);

        cargarGuias();

        // Guardar los cambios de distance_km
        btnGuardarCambios.setOnClickListener(v -> {
            for (Map.Entry<Integer, EditText> entry : distanciaMap.entrySet()) {
                int guiaId = entry.getKey();
                String texto = entry.getValue().getText().toString().trim();
                if (!texto.isEmpty()) {
                    try {
                        double distancia = Double.parseDouble(texto);
                        db.getWritableDatabase().execSQL(
                                "UPDATE guides SET distance_km = ? WHERE id = ?",
                                new Object[]{distancia, guiaId}
                        );
                    } catch (NumberFormatException e) {
                        Utils.toast(this, "Valor inválido para la guía ID " + guiaId);
                    }
                }
            }
            Utils.toast(this, "Distancias actualizadas correctamente.");
        });
    }

    private void cargarGuias() {
        layoutGuias.removeAllViews();
        distanciaMap.clear();

        // Generar guías faltantes automáticamente
        Cursor cShipments = db.raw(
                "SELECT id FROM shipments WHERE id NOT IN (SELECT shipment_id FROM guides)", null);
        if (cShipments.moveToFirst()) {
            do {
                int shipmentId = cShipments.getInt(0);
                db.getWritableDatabase().execSQL(
                        "INSERT INTO guides (shipment_id, guide_number, distance_km) VALUES (?, ?, ?)",
                        new Object[]{shipmentId, "GEN" + shipmentId, 0}
                );
            } while (cShipments.moveToNext());
        }
        cShipments.close();

        // Obtener todas las guías con info completa
        Cursor c = db.raw(
                "SELECT g.id, g.guide_number, s.shipment_code, s.receiver_address, s.weight_kg, s.volume_m3, g.distance_km " +
                        "FROM guides g " +
                        "JOIN shipments s ON g.shipment_id = s.id " +
                        "ORDER BY g.id ASC", null);

        if (c.moveToFirst()) {
            do {
                int guiaId = c.getInt(0);
                String info = "Guía: " + c.getString(1) +
                        " | Código: " + c.getString(2) +
                        " | Destino: " + c.getString(3) +
                        " | Peso: " + c.getDouble(4) + " kg" +
                        " | Volumen: " + c.getDouble(5) + " m³";

                // Crear EditText para editar distance_km
                EditText etDistancia = new EditText(this);
                etDistancia.setText(String.valueOf(c.getDouble(6)));
                etDistancia.setHint("Distancia km");
                etDistancia.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

                distanciaMap.put(guiaId, etDistancia);

                // Layout horizontal para mostrar info + EditText
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(10, 10, 10, 10);

                android.widget.TextView tvInfo = new android.widget.TextView(this);
                tvInfo.setText(info);

                row.addView(tvInfo);
                row.addView(etDistancia);

                layoutGuias.addView(row);

            } while (c.moveToNext());
        }

        c.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.closeDB();
    }
}
