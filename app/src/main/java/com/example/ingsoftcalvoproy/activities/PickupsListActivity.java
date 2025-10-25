package com.example.ingsoftcalvoproy.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;
import com.example.ingsoftcalvoproy.utils.Utils;

import java.util.ArrayList;

/**
 * Lista de solicitudes de recogida (pickups).
 * Muestra las solicitudes del usuario o del recolector asignado.
 */
public class PickupsListActivity extends AppCompatActivity {

    private Db db;
    private ListView lvPickups;
    private ArrayList<String> data = new ArrayList<>();
    private ArrayList<Integer> ids = new ArrayList<>();

    // 🔹 Cambia este valor según quién acceda (simulación temporal)
    private boolean isCollectorView = false; // false = usuario, true = recolector
    private int currentUserId = 1;           // Ejemplo fijo, se integrará con sesión luego

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickups_list);

        db = new Db(this);
        lvPickups = findViewById(R.id.lvPickups);

        loadPickups();

        // 🔹 Permite marcar recogida como completada (solo recolector)
        lvPickups.setOnItemClickListener((parent, view, position, id) -> {
            if (isCollectorView) {
                int pickupId = ids.get(position);
                db.updatePickupStatus(pickupId, "COLECTADA");
                Utils.toast(this, "✅ Recogida marcada como completada.");
                loadPickups();
            }
        });
    }

    private void loadPickups() {
        data.clear();
        ids.clear();

        Cursor c;
        if (isCollectorView) {
            // 🔹 Vista del RECOLECTOR
            c = db.raw("""
                SELECT id, address, status, datetime(created_at)
                FROM pickups
                WHERE collector_id = ?
                ORDER BY id DESC
            """, new String[]{String.valueOf(currentUserId)});
        } else {
            // 🔹 Vista del USUARIO
            c = db.raw("""
                SELECT id, address, status, datetime(created_at)
                FROM pickups
                WHERE user_id = ?
                ORDER BY id DESC
            """, new String[]{String.valueOf(currentUserId)});
        }

        while (c.moveToNext()) {
            int id = c.getInt(0);
            String address = c.getString(1);
            String status = Utils.formatStatus(c.getString(2));
            String date = c.getString(3);

            data.add("ID: " + id +
                    "\nDirección: " + address +
                    "\nEstado: " + status +
                    "\nFecha: " + date);
            ids.add(id);
        }
        c.close();

        lvPickups.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.closeDB();
    }
}
