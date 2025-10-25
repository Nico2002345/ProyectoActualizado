package com.example.ingsoftcalvoproy.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;
import com.example.ingsoftcalvoproy.utils.Utils;

/**
 * Pantalla principal del repartidor o conductor.
 * Muestra su camión, capacidad y guías asignadas.
 */
public class CourierHomeActivity extends AppCompatActivity {

    private Db db;
    private TextView tvTruckInfo, tvStats;
    private Button btnVerGuias, btnRegistrarEntrega, btnActualizarEstado;

    private int userId;
    private String userName;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_home);

        db = new Db(this);

        // === Vistas (IDs definidos en el XML) ===
        tvTruckInfo = findViewById(R.id.tvTruckInfo);
        tvStats = findViewById(R.id.tvStats);
        btnVerGuias = findViewById(R.id.btnVerGuias);
        btnRegistrarEntrega = findViewById(R.id.btnRegistrarEntrega);
        btnActualizarEstado = findViewById(R.id.btnActualizarEstado);

        // === Datos del usuario ===
        userId = getIntent().getIntExtra("USER_ID", -1);
        userName = getIntent().getStringExtra("USER_NAME");
        userRole = getIntent().getStringExtra("USER_ROLE");

        setTitle("Panel del " + (userRole != null ? userRole : "Repartidor"));

        loadTruckData();
        loadStats();

        // === Acciones de botones ===
        btnVerGuias.setOnClickListener(v ->
                startActivity(new Intent(this, GuidesListActivity.class))
        );

        btnRegistrarEntrega.setOnClickListener(v ->
                startActivity(new Intent(this, TrackingActivity.class))
        );

        btnActualizarEstado.setOnClickListener(v -> markDelivered());
    }

    /** Carga información del camión asignado */
    private void loadTruckData() {
        Cursor c = db.raw("""
            SELECT plate, capacity_kg
            FROM trucks
            WHERE active = 1
            ORDER BY id ASC
            LIMIT 1
        """, null);

        if (c.moveToFirst()) {
            String plate = c.getString(0);
            double capacity = c.getDouble(1);
            tvTruckInfo.setText("🚚 Camión asignado: " + plate +
                    "\nCapacidad: " + capacity + " kg");
        } else {
            tvTruckInfo.setText("🚫 No hay camiones activos asignados.");
        }
        c.close();
    }

    /** Muestra estadísticas de envíos */
    private void loadStats() {
        Cursor c = db.raw("""
            SELECT 
                SUM(CASE WHEN status='CREADO' THEN 1 ELSE 0 END) AS creados,
                SUM(CASE WHEN status='EN_TRÁNSITO' THEN 1 ELSE 0 END) AS transito,
                SUM(CASE WHEN status='ENTREGADO' THEN 1 ELSE 0 END) AS entregados
            FROM shipments
        """, null);

        if (c.moveToFirst()) {
            int creados = c.getInt(0);
            int transito = c.getInt(1);
            int entregados = c.getInt(2);
            tvStats.setText("📦 Envíos:\n" +
                    "Creados: " + creados + "\n" +
                    "En tránsito: " + transito + "\n" +
                    "Entregados: " + entregados);
        } else {
            tvStats.setText("No hay datos disponibles.");
        }
        c.close();
    }

    /** Marca aleatoriamente un envío en tránsito como entregado */
    private void markDelivered() {
        Cursor c = db.raw("""
            SELECT id, shipment_code
            FROM shipments
            WHERE status = 'EN_TRÁNSITO'
            ORDER BY RANDOM()
            LIMIT 1
        """, null);

        if (c.moveToFirst()) {
            int shipmentId = c.getInt(0);
            String code = c.getString(1);

            ContentValues cv = new ContentValues();
            cv.put("status", "ENTREGADO");
            db.update("shipments", cv, "id=?", new String[]{String.valueOf(shipmentId)});

            ContentValues ev = new ContentValues();
            ev.put("shipment_id", shipmentId);
            ev.put("status", "ENTREGADO");
            ev.put("location", "Destino final");
            db.insert("tracking_events", ev);

            Utils.toastLong(this, "✅ Entrega confirmada para " + code);
            loadStats();
        } else {
            Utils.toast(this, "No hay envíos en tránsito para entregar.");
        }
        c.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.closeDB();
    }
}
