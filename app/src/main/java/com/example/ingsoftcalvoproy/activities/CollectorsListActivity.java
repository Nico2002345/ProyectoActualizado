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
 * Lista de recolectores (collectors).
 * Permite visualizar la información básica y prepararse para asignaciones.
 */
public class CollectorsListActivity extends AppCompatActivity {

    private Db db;
    private ListView lvCollectors;
    private ArrayList<String> data = new ArrayList<>();
    private ArrayList<Integer> ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collectors_list);

        db = new Db(this);
        lvCollectors = findViewById(R.id.lvCollectors);

        loadCollectors();

        // 🔹 Acción al hacer clic sobre un recolector (opcional)
        lvCollectors.setOnItemClickListener((parent, view, position, id) -> {
            int collectorId = ids.get(position);
            Utils.toast(this, "Seleccionaste al recolector ID: " + collectorId);
            // 🔸 Aquí podrías abrir una pantalla para asignarle una ruta o pickup
        });
    }

    private void loadCollectors() {
        data.clear();
        ids.clear();

        Cursor c = db.raw("""
            SELECT id, name, phone, email, active
            FROM collectors
            ORDER BY name ASC
        """, null);

        while (c.moveToNext()) {
            int id = c.getInt(0);
            String name = c.getString(1);
            String phone = c.getString(2);
            String email = c.getString(3);
            int active = c.getInt(4);

            String status = active == 1 ? "🟢 Activo" : "🔴 Inactivo";
            data.add("ID: " + id +
                    "\nNombre: " + name +
                    "\nTeléfono: " + (phone != null ? phone : "N/D") +
                    "\nEmail: " + (email != null ? email : "N/D") +
                    "\nEstado: " + status);
            ids.add(id);
        }
        c.close();

        lvCollectors.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.closeDB();
    }
}
