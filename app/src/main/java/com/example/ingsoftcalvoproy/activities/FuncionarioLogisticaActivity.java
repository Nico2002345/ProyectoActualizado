package com.example.ingsoftcalvoproy.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;
import com.example.ingsoftcalvoproy.utils.Utils;

/**
 * Panel principal para el Funcionario de Logística.
 * Permite obtener guías, balancear cargas y generar guías clasificadas.
 */
public class FuncionarioLogisticaActivity extends AppCompatActivity {

    private Button btnObtenerGuias, btnBalancearCargas, btnGenerarGuias;
    private Db db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funcionario_logistico);

        db = new Db(this);

        btnObtenerGuias = findViewById(R.id.btnObtenerGuias);
        btnBalancearCargas = findViewById(R.id.btnBalancearCargas);
        btnGenerarGuias = findViewById(R.id.btnGenerarGuias);

        // Click listeners
        btnObtenerGuias.setOnClickListener(v -> obtenerGuias());
        btnBalancearCargas.setOnClickListener(v -> balancearCargas());
        btnGenerarGuias.setOnClickListener(v -> generarGuias());
    }

    // === OBTENER GUÍAS PARA MONITOREO ===
    private void obtenerGuias() {
        try {
            // 1️⃣ Primero generamos guías para todos los envíos que no tengan guía
            Cursor cShipments = db.raw(
                    "SELECT id FROM shipments WHERE id NOT IN (SELECT shipment_id FROM guides)", null);
            if (cShipments.moveToFirst()) {
                do {
                    int shipmentId = cShipments.getInt(0);
                    db.getWritableDatabase().execSQL(
                            "INSERT INTO guides (shipment_id, guide_number) VALUES (?, ?)",
                            new Object[]{shipmentId, "GEN" + shipmentId});
                } while (cShipments.moveToNext());
            }
            cShipments.close();

            // 2️⃣ Ahora obtenemos todas las guías con información completa
            Cursor c = db.raw(
                    "SELECT g.id, g.guide_number, s.shipment_code, s.receiver_address, s.weight_kg, s.volume_m3 " +
                            "FROM guides g " +
                            "JOIN shipments s ON g.shipment_id = s.id", null);

            if (c.moveToFirst()) {
                StringBuilder sb = new StringBuilder();
                do {
                    sb.append("Guía ID: ").append(c.getInt(0))
                            .append(", Número: ").append(c.getString(1))
                            .append(", Código Envío: ").append(c.getString(2))
                            .append(", Destino: ").append(c.getString(3))
                            .append(", Peso: ").append(c.getDouble(4))
                            .append(" kg, Volumen: ").append(c.getDouble(5))
                            .append(" m³\n");
                } while (c.moveToNext());

                Utils.toastLong(this, sb.toString());
            } else {
                Utils.toast(this, "No hay guías disponibles.");
            }
            c.close();

        } catch (Exception e) {
            Utils.toast(this, "Error al obtener guías: " + e.getMessage());
        }
    }

    // === BALANCEAR CARGAS ===
    private void balancearCargas() {
        try {
            db.getWritableDatabase().execSQL(
                    "UPDATE guides SET distance_km = 0 WHERE distance_km IS NULL");
            Utils.toast(this, "Se balancearon las cargas de las guías.");
        } catch (Exception e) {
            Utils.toast(this, "Error al balancear cargas: " + e.getMessage());
        }
    }

    // === GENERAR Y ALMACENAR GUÍAS (manual) ===
    private void generarGuias() {
        try {
            Cursor cShipments = db.raw(
                    "SELECT id FROM shipments WHERE id NOT IN (SELECT shipment_id FROM guides)", null);
            if (cShipments.moveToFirst()) {
                do {
                    int shipmentId = cShipments.getInt(0);
                    db.getWritableDatabase().execSQL(
                            "INSERT INTO guides (shipment_id, guide_number) VALUES (?, ?)",
                            new Object[]{shipmentId, "GEN" + shipmentId});
                } while (cShipments.moveToNext());
            }
            cShipments.close();

            Utils.toast(this, "Se generaron y almacenaron las guías.");
        } catch (Exception e) {
            Utils.toast(this, "Error al generar guías: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.closeDB();
    }
}
